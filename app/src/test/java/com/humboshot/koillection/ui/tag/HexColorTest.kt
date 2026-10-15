package com.humboshot.koillection.ui.tag

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HexColorTest {
    @Test
    fun readsWithAndWithoutHash() {
        assertEquals(Color(0xFF009688), parseHexColor("#009688"))
        assertEquals(Color(0xFF009688), parseHexColor("009688"))
    }

    @Test
    fun rejectsOtherValues() {
        assertNull(parseHexColor(null))
        assertNull(parseHexColor("#fff"))
        assertNull(parseHexColor("teal"))
        assertNull(parseHexColor("#00968z"))
    }
}
