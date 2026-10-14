package com.humboshot.koillection.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import javax.crypto.AEADBadTagException
import javax.crypto.KeyGenerator

/** Uses a software key. The Android Keystore key itself can only be tested on a device. */
class SecretCipherTest {
    private val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    private val cipher = SecretCipher { key }

    @Test
    fun roundTrip() {
        assertEquals("p4ssw0rd ✓", cipher.decrypt(cipher.encrypt("p4ssw0rd ✓")))
    }

    @Test
    fun sameValueEncryptsDifferently() {
        assertNotEquals(cipher.encrypt("secret"), cipher.encrypt("secret"))
    }

    @Test
    fun otherKeyCannotDecrypt() {
        val otherKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

        assertThrows(AEADBadTagException::class.java) {
            SecretCipher { otherKey }.decrypt(cipher.encrypt("secret"))
        }
    }
}
