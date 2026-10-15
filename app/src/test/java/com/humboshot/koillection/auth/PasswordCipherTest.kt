package com.humboshot.koillection.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.security.KeyPairGenerator

/** Uses a software key pair. The Android Keystore key itself can only be tested on a device. */
class PasswordCipherTest {
    private fun keyPair() = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

    private val keys = keyPair()

    @Test
    fun roundTrip() {
        val encrypted = PasswordCipher.encrypt("p4ssw0rd ✓", keys.public)

        assertEquals("p4ssw0rd ✓", PasswordCipher.decrypt(encrypted, keys.private))
    }

    @Test
    fun sameValueEncryptsDifferently() {
        assertNotEquals(PasswordCipher.encrypt("secret", keys.public), PasswordCipher.encrypt("secret", keys.public))
    }

    @Test
    fun otherKeyCannotDecrypt() {
        val encrypted = PasswordCipher.encrypt("secret", keys.public)

        assertThrows(Exception::class.java) { PasswordCipher.decrypt(encrypted, keyPair().private) }
    }
}
