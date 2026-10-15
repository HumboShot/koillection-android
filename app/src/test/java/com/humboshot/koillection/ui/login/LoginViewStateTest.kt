package com.humboshot.koillection.ui.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginViewStateTest {
    @Test
    fun toServerInput_removesHttpsAndSlash() {
        assertEquals("collection.example.com", "https://collection.example.com/".toServerInput())
    }

    @Test
    fun toServerInput_keepsHttp() {
        assertEquals("http://192.168.1.2:8080", "http://192.168.1.2:8080/".toServerInput())
    }

    @Test
    fun canLogin_needsAllFields() {
        assertFalse(LoginViewState(server = "a.com", username = "user").canLogin)
        assertFalse(LoginViewState(server = " ", username = "user", password = "x").canLogin)
        assertTrue(LoginViewState(server = "a.com", username = "user", password = " ").canLogin)
    }
}
