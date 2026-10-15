package com.humboshot.koillection.ui.album

import com.humboshot.koillection.data.album.Photo
import com.humboshot.koillection.data.album.PhotoWrite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PhotoFormFieldsTest {
    private val original = Photo(id = "p1", title = "Beach", place = "Skagen", visibility = "public")

    @Test
    fun validate_needsTitle() {
        assertNotNull(PhotoFormFields(title = " ").validate())
        assertNull(PhotoFormFields(title = "Beach").validate())
    }

    @Test
    fun toCreate_buildsAlbumIriAndSkipsEmptyTexts() {
        assertEquals(
            PhotoWrite(title = "Beach", place = "Skagen", album = "/api/albums/a1", visibility = "public"),
            PhotoFormFields(title = "Beach ", comment = " ", place = "Skagen").toCreate("a1")
        )
    }

    @Test
    fun toPatch_sendsOnlyChanges() {
        val fields = PhotoFormFields.from(original)

        assertNull(fields.toPatch(original))
        assertEquals(PhotoWrite(place = ""), fields.copy(place = "").toPatch(original))
        assertEquals(PhotoWrite(comment = "Sunset"), fields.copy(comment = "Sunset").toPatch(original))
    }
}
