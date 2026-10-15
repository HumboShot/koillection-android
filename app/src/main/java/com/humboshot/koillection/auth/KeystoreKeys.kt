package com.humboshot.koillection.auth

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.ProviderException
import java.security.PublicKey
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/** The Android Keystore keys of the saved login. The keys cannot leave the device. */
internal class KeystoreKeys(private val context: Context) {
    private val keyStore: KeyStore by lazy { KeyStore.getInstance(PROVIDER).apply { load(null) } }

    fun has(alias: String): Boolean = keyStore.containsAlias(alias)

    fun delete(alias: String) {
        runCatching { keyStore.deleteEntry(alias) }
    }

    fun secretKey(alias: String): SecretKey? = keyStore.getKey(alias, null) as? SecretKey

    /** The AES key for the server, username and token. It works only while the device is unlocked. */
    fun valueKey(): SecretKey {
        secretKey(VALUE_KEY_ALIAS)?.let { return it }
        return withStrongBox { strongBox ->
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
            generator.init(
                KeyGenParameterSpec.Builder(VALUE_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .hardened(strongBox)
                    .build()
            )
            generator.generateKey()
        }
    }

    /**
     * The public half of the RSA key for the password. Encryption needs no authentication, so a login
     * can save the password without a prompt. Only decryption needs the fingerprint or the screen lock.
     */
    fun passwordPublicKey(): PublicKey {
        keyStore.getCertificate(PASSWORD_KEY_ALIAS)?.let { return it.publicKey }
        return withStrongBox { strongBox ->
            val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, PROVIDER)
            generator.initialize(
                KeyGenParameterSpec.Builder(PASSWORD_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setKeySize(2048)
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                    .setUserAuthenticationRequired(true)
                    .authenticationTimeout()
                    .hardened(strongBox)
                    .build()
            )
            generator.generateKeyPair().public
        }
    }

    fun passwordPrivateKey(): PrivateKey? = keyStore.getKey(PASSWORD_KEY_ALIAS, null) as? PrivateKey

    private fun KeyGenParameterSpec.Builder.authenticationTimeout(): KeyGenParameterSpec.Builder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setUserAuthenticationParameters(
                AUTH_SECONDS,
                KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
            )
        } else {
            @Suppress("DEPRECATION")
            setUserAuthenticationValidityDurationSeconds(AUTH_SECONDS)
        }

    private fun KeyGenParameterSpec.Builder.hardened(strongBox: Boolean): KeyGenParameterSpec.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setUnlockedDeviceRequired(true)
            setIsStrongBoxBacked(strongBox)
        }
        return this
    }

    /** Tries the StrongBox chip first. Many devices do not have one. */
    private fun <T> withStrongBox(generate: (strongBox: Boolean) -> T): T {
        val hasStrongBox = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        if (!hasStrongBox) return generate(false)
        return try {
            generate(true)
        } catch (e: ProviderException) {
            // StrongBoxUnavailableException is a ProviderException. The class does not exist below Android 9.
            generate(false)
        }
    }

    companion object {
        private const val PROVIDER = "AndroidKeyStore"
        const val LEGACY_VALUE_KEY_ALIAS = "koillection_auth_key"
        const val VALUE_KEY_ALIAS = "koillection_auth_key_v2"
        const val PASSWORD_KEY_ALIAS = "koillection_password_key"

        /** How long the password key stays usable after the prompt. */
        private const val AUTH_SECONDS = 30
    }
}
