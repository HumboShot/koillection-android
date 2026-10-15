package com.humboshot.koillection.data

import com.humboshot.koillection.data.album.Photo
import com.humboshot.koillection.data.repository.sortPhotos
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoTest {
    @Test
    fun sortPhotos_byDateThenUndatedLast() {
        val undated = Photo(id = "1", title = "A")
        val newer = Photo(id = "2", title = "B", takenAt = "2026-05-01T00:00:00+00:00")
        val older = Photo(id = "3", title = "C", takenAt = "2025-01-01T00:00:00+00:00")

        assertEquals(listOf(older, newer, undated), sortPhotos(listOf(undated, newer, older)))
    }
}
