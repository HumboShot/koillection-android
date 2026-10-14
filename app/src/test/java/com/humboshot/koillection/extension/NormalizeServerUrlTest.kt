package com.humboshot.koillection.extension

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NormalizeServerUrlTest {
    private val expected = "https://collection.humboshot.com/"

    @Test
    fun addsSchemeAndTrailingSlash() {
        assertEquals(expected, "collection.humboshot.com".normalizeServerUrl())
    }

    @Test
    fun acceptsCapitalizedScheme() {
        assertEquals(expected, "Https://collection.humboshot.com".normalizeServerUrl())
    }

    @Test
    fun acceptsSchemeMissingColon() {
        assertEquals(expected, "https//collection.humboshot.com".normalizeServerUrl())
    }

    @Test
    fun trimsWhitespaceAndTrailingSlashes() {
        assertEquals(expected, "  https://collection.humboshot.com//  ".normalizeServerUrl())
    }

    @Test
    fun keepsExplicitHttp() {
        assertEquals("http://192.168.1.10:8080/", "http://192.168.1.10:8080".normalizeServerUrl())
    }

    @Test
    fun rejectsDoubledScheme() {
        assertNull("https://https://collection.humboshot.com".normalizeServerUrl())
    }

    @Test
    fun rejectsGarbage() {
        assertNull("".normalizeServerUrl())
        assertNull("not a url".normalizeServerUrl())
    }
}
