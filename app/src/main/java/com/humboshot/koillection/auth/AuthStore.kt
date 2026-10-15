package com.humboshot.koillection.auth

import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.core.content.edit
import com.humboshot.koillection.MainApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.security.KeyStore

/**
 * Keeps the server, username, password and token. Only encrypted values go to disk.
 *
 * - The server, username and token use an AES key in the Android Keystore. The key works only
 *   while the device is unlocked.
 * - With fingerprint unlock on, the password uses an RSA key that needs the fingerprint or the
 *   screen lock. The user unlocks it once per app start with [unlock].
 * - With fingerprint unlock off, or without a screen lock, the password uses the AES key.
 *
 * Keystore and disk work is slow, so call [load], [signIn], [unlock] and [setFingerprintUnlock]
 * from a background thread.
 */
object AuthStore : Session {
    private const val PREFS_NAME = "koillection_auth"
    private const val KEY_SERVER = "server"
    private const val KEY_USERNAME = "username"
    private const val KEY_LOCKED_PASSWORD = "password_rsa"
    private const val KEY_PASSWORD = "password_aes"
    private const val KEY_TOKEN = "token"
    private const val LEGACY_KEY_PASSWORD = "password"

    /** App settings stay after a logout, so they have their own file. */
    private const val SETTINGS_NAME = "koillection_settings"
    private const val KEY_FINGERPRINT_UNLOCK = "fingerprint_unlock"
    private const val KEY_LAST_SERVER = "last_server"

    private const val LEGACY_PREFS_NAME = "koillection_shared_prefs"
    private const val LEGACY_KEY_ALIAS = "_androidx_security_master_key_"

    private data class Stored(val serverUrl: String, val username: String, val password: String?, val token: String)

    private val context: Context get() = MainApplication.applicationContext
    private val prefs: SharedPreferences by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    private val settings: SharedPreferences by lazy { context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE) }
    private val keys by lazy { KeystoreKeys(context) }
    private val cipher = SecretCipher { keys.valueKey() }

    @Volatile
    private var loaded = false

    @Volatile
    private var stored: Stored? = null

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    private val _isLocked = MutableStateFlow(false)
    private val _fingerprintUnlock = MutableStateFlow(true)

    /** Null until [load] finishes. */
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    /** True while a saved password waits for [unlock]. */
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    /** True when the password is locked behind the fingerprint or the screen lock. */
    val fingerprintUnlock: StateFlow<Boolean> = _fingerprintUnlock.asStateFlow()

    override val serverUrl: String? get() = current()?.serverUrl
    override val username: String? get() = current()?.username
    override val password: String? get() = current()?.password
    override val token: String? get() = current()?.token

    /** The server of the last login, for the login screen. A server address is not secret. */
    val lastServer: String? get() = settings.getString(KEY_LAST_SERVER, null)

    /** Fingerprint unlock needs a screen lock (PIN, pattern or password). */
    val isDeviceSecure: Boolean
        get() = keyguard.isDeviceSecure

    private val keyguard: KeyguardManager get() = context.getSystemService(KeyguardManager::class.java)

    private fun current(): Stored? {
        if (!loaded) load()
        return stored
    }

    @Synchronized
    fun load() {
        if (loaded) return
        removeLegacyStorage()
        migrateLegacyKey()
        stored = readStored()
        stored?.let { if (lastServer == null) settings.edit { putString(KEY_LAST_SERVER, it.serverUrl) } }
        loaded = true
        _fingerprintUnlock.value = wantsFingerprintUnlock()
        _isLocked.value = stored != null && prefs.contains(KEY_LOCKED_PASSWORD)
        _isLoggedIn.value = stored != null
    }

    @Synchronized
    fun signIn(serverUrl: String, username: String, password: String, token: String) {
        prefs.edit {
            putString(KEY_SERVER, cipher.encrypt(serverUrl))
            putString(KEY_USERNAME, cipher.encrypt(username))
            putString(KEY_TOKEN, cipher.encrypt(token))
            putPassword(password)
        }
        settings.edit { putString(KEY_LAST_SERVER, serverUrl) }
        stored = Stored(serverUrl, username, password, token)
        loaded = true
        _isLocked.value = false
        _isLoggedIn.value = true
    }

    /**
     * Decrypts the saved password. Call it right after the fingerprint or screen lock prompt succeeds.
     * When the key is gone, for example after a new fingerprint, the user must log in again.
     */
    @Synchronized
    fun unlock(): UnlockResult {
        val existing = stored ?: return UnlockResult.LoggedOut
        val encrypted = prefs.getString(KEY_LOCKED_PASSWORD, null)
        val privateKey = runCatching { keys.passwordPrivateKey() }.getOrNull()
        if (encrypted == null || privateKey == null) {
            signOut()
            return UnlockResult.LoggedOut
        }
        return try {
            stored = existing.copy(password = PasswordCipher.decrypt(encrypted, privateKey))
            _isLocked.value = false
            UnlockResult.Unlocked
        } catch (e: KeyPermanentlyInvalidatedException) {
            signOut()
            UnlockResult.LoggedOut
        } catch (e: Exception) {
            UnlockResult.Failed
        }
    }

    /** Saves the setting and stores the password again with the matching key. The app must be unlocked. */
    @Synchronized
    fun setFingerprintUnlock(enabled: Boolean) {
        if (enabled && !isDeviceSecure) return
        settings.edit { putBoolean(KEY_FINGERPRINT_UNLOCK, enabled) }
        _fingerprintUnlock.value = enabled
        val password = stored?.password ?: return
        prefs.edit { putPassword(password) }
    }

    @Synchronized
    override fun updateToken(token: String) {
        val existing = stored ?: return
        stored = existing.copy(token = token)
        // The value key does not work while the device is locked. Then only this app run keeps the new token.
        runCatching { prefs.edit { putString(KEY_TOKEN, cipher.encrypt(token)) } }
    }

    @Synchronized
    override fun signOut() {
        prefs.edit { clear() }
        stored = null
        loaded = true
        _isLocked.value = false
        _isLoggedIn.value = false
    }

    private fun wantsFingerprintUnlock(): Boolean = isDeviceSecure && settings.getBoolean(KEY_FINGERPRINT_UNLOCK, true)

    /** Writes the password with the key that the setting asks for, and removes the other copy. */
    private fun SharedPreferences.Editor.putPassword(password: String) {
        val locked = if (wantsFingerprintUnlock()) {
            runCatching { PasswordCipher.encrypt(password, keys.passwordPublicKey()) }.getOrNull()
        } else {
            null
        }
        if (locked != null) {
            putString(KEY_LOCKED_PASSWORD, locked)
            remove(KEY_PASSWORD)
        } else {
            putString(KEY_PASSWORD, cipher.encrypt(password))
            remove(KEY_LOCKED_PASSWORD)
        }
    }

    private fun readStored(): Stored? {
        val values = listOf(KEY_SERVER, KEY_USERNAME, KEY_TOKEN).map { prefs.getString(it, null) }
        if (values.any { it == null }) return null

        return runCatching {
            val (server, username, token) = values.map { cipher.decrypt(it!!) }
            Stored(server, username, prefs.getString(KEY_PASSWORD, null)?.let(cipher::decrypt), token)
        }.getOrElse {
            // A restored backup or a reset lock screen can make the key unusable. Then the user must log in again.
            // A locked device only blocks the key for now, so keep the data.
            if (!keyguard.isDeviceLocked) prefs.edit { clear() }
            null
        }
    }

    /**
     * Moves the data of the first Keystore key to the current keys. The old key had no device-unlock rule,
     * and it kept the password without the fingerprint or screen lock.
     */
    private fun migrateLegacyKey() {
        // The new value key does not work while the device is locked. Try again at the next start.
        if (keyguard.isDeviceLocked) return
        val legacyKey = runCatching { keys.secretKey(KeystoreKeys.LEGACY_VALUE_KEY_ALIAS) }.getOrNull() ?: return
        val legacy = SecretCipher { legacyKey }
        runCatching {
            val values = listOf(KEY_SERVER, KEY_USERNAME, LEGACY_KEY_PASSWORD, KEY_TOKEN)
                .map { prefs.getString(it, null)?.let(legacy::decrypt) }
            val (server, username, password, token) = values
            prefs.edit { clear() }
            if (server != null && username != null && password != null && token != null) {
                prefs.edit {
                    putString(KEY_SERVER, cipher.encrypt(server))
                    putString(KEY_USERNAME, cipher.encrypt(username))
                    putString(KEY_TOKEN, cipher.encrypt(token))
                    putPassword(password)
                }
            }
        }.onFailure { prefs.edit { clear() } }
        keys.delete(KeystoreKeys.LEGACY_VALUE_KEY_ALIAS)
    }

    /** Removes the data of the old EncryptedSharedPreferences storage, if it is still there. */
    private fun removeLegacyStorage() {
        val legacyFile = File(context.dataDir, "shared_prefs/$LEGACY_PREFS_NAME.xml")
        if (!legacyFile.exists()) return

        context.deleteSharedPreferences(LEGACY_PREFS_NAME)
        runCatching {
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(LEGACY_KEY_ALIAS)
        }
    }
}

enum class UnlockResult { Unlocked, Failed, LoggedOut }
