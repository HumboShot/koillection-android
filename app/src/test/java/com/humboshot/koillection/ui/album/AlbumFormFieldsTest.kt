package com.humboshot.koillection.ui.album

import com.humboshot.koillection.data.album.Album
import com.humboshot.koillection.data.album.AlbumWrite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AlbumFormFieldsTest {
    private val original = Album(id = "a1", title = "Holiday", visibility = "public")

    @Test
    fun validate_needsTitle() {
        assertNotNull(AlbumFormFields(title = " ").validate())
        assertNull(AlbumFormFields(title = "Holiday").validate())
    }

    @Test
    fun toCreate_setsParentIriOnlyForSubAlbums() {
        assertEquals(AlbumWrite(title = "Holiday", visibility = "public"), AlbumFormFields(title = "Holiday ").toCreate(null))
        assertEquals("/api/albums/p1", AlbumFormFields(title = "Day 1").toCreate("p1").parent)
    }

    @Test
    fun toPatch_sendsOnlyChanges() {
        val fields = AlbumFormFields.from(original)

        assertNull(fields.toPatch(original))
        assertEquals(AlbumWrite(title = "Trip"), fields.copy(title = "Trip").toPatch(original))
    }
}
