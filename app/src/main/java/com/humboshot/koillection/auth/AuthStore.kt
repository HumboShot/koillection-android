package com.humboshot.koillection.auth

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import com.humboshot.koillection.MainApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Keeps the server, username, password and token. Each value is encrypted with an
 * AES key in the Android Keystore, so the key cannot leave the device. The values are
 * decrypted once and kept in memory.
 *
 * Keystore and disk work is slow, so call [load] and [signIn] from a background thread.
 */
object AuthStore : Session {
    private const val PREFS_NAME = "koillection_auth"
    private const val KEY_ALIAS = "koillection_auth_key"
    private const val KEY_SERVER = "server"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"
    private const val KEY_TOKEN = "token"

    private const val LEGACY_PREFS_NAME = "koillection_shared_prefs"
    private const val LEGACY_KEY_ALIAS = "_androidx_security_master_key_"

    private data class Stored(val serverUrl: String, val username: String, val password: String, val token: String)

    private val context: Context get() = MainApplication.applicationContext
    private val prefs: SharedPreferences by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    private val key: SecretKey by lazy { keystoreKey() }
    private val cipher = SecretCipher { key }

    @Volatile
    private var loaded = false

    @Volatile
    private var stored: Stored? = null

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)

    /** Null until [load] finishes. */
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    override val serverUrl: String? get() = current()?.serverUrl
    override val username: String? get() = current()?.username
    override val password: String? get() = current()?.password
    override val token: String? get() = current()?.token

    private fun current(): Stored? {
        if (!loaded) load()
        return stored
    }

    @Synchronized
    fun load() {
        if (loaded) return
        removeLegacyStorage()
        stored = readStored()
        loaded = true
        _isLoggedIn.value = stored != null
    }

    @Synchronized
    fun signIn(serverUrl: String, username: String, password: String, token: String) {
        prefs.edit {
            putString(KEY_SERVER, cipher.encrypt(serverUrl))
            putString(KEY_USERNAME, cipher.encrypt(username))
            putString(KEY_PASSWORD, cipher.encrypt(password))
            putString(KEY_TOKEN, cipher.encrypt(token))
        }
        stored = Stored(serverUrl, username, password, token)
        loaded = true
        _isLoggedIn.value = true
    }

    @Synchronized
    override fun updateToken(token: String) {
        val existing = stored ?: return
        prefs.edit { putString(KEY_TOKEN, cipher.encrypt(token)) }
        stored = existing.copy(token = token)
    }

    @Synchronized
    override fun signOut() {
        prefs.edit { clear() }
        stored = null
        loaded = true
        _isLoggedIn.value = false
    }

    private fun readStored(): Stored? {
        val values = listOf(KEY_SERVER, KEY_USERNAME, KEY_PASSWORD, KEY_TOKEN).map { prefs.getString(it, null) }
        if (values.any { it == null }) return null

        // A restored backup or a reset lock screen can make the key unusable. Then the user must log in again.
        return runCatching {
            val (server, username, password, token) = values.map { cipher.decrypt(it!!) }
            Stored(server, username, password, token)
        }.getOrElse {
            prefs.edit { clear() }
            null
        }
    }

    private fun keystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
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
