package com.humboshot.koillection.data

import com.humboshot.koillection.data.repository.imagePart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImagePartTest {
    @Test
    fun imagePart_usesFileFieldAndMimeType() {
        val part = imagePart(byteArrayOf(1, 2, 3), "image/png")

        val disposition = part.headers?.get("Content-Disposition").orEmpty()
        assertTrue(disposition, disposition.contains("name=\"file\""))
        assertTrue(disposition, disposition.contains("filename=\"upload.png\""))
        assertEquals("image/png", part.body.contentType().toString())
        assertEquals(3L, part.body.contentLength())
    }

    @Test
    fun imagePart_defaultsToJpgWithoutMimeType() {
        val disposition = imagePart(byteArrayOf(1), null).headers?.get("Content-Disposition").orEmpty()

        assertTrue(disposition, disposition.contains("filename=\"upload.jpg\""))
    }
}
