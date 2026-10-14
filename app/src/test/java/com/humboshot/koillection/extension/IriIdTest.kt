package com.humboshot.koillection.extension

import org.junit.Assert.assertEquals
import org.junit.Test

class IriIdTest {
    @Test
    fun iriId_returnsLastPathSegment() {
        assertEquals(
            "0190f5c1-1234-7abc-8def-0123456789ab",
            "/api/collections/0190f5c1-1234-7abc-8def-0123456789ab".iriId()
        )
    }

    @Test
    fun iriId_returnsInputWhenNoSlash() {
        assertEquals("abc", "abc".iriId())
    }
}
