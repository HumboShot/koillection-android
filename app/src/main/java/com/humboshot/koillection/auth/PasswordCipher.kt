package com.humboshot.koillection.auth

import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

/** Encrypts the password with RSA-OAEP. The stored form is base64(ciphertext). */
object PasswordCipher {
    private const val TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"

    // The Android Keystore supports MGF1 only with SHA-1, so both sides must name it.
    private val OAEP = OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)

    fun encrypt(plain: String, publicKey: PublicKey): String {
        // A copy of the key outside the Keystore, because Keystore public keys reject some providers.
        val key = KeyFactory.getInstance(publicKey.algorithm).generatePublic(X509EncodedKeySpec(publicKey.encoded))
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, OAEP)
        return cipher.doFinal(plain.toByteArray()).toByteString().base64()
    }

    fun decrypt(encoded: String, privateKey: PrivateKey): String {
        val bytes = checkNotNull(encoded.decodeBase64()) { "Stored value is not base64" }.toByteArray()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP)
        return String(cipher.doFinal(bytes))
    }
}
