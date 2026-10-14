package com.humboshot.koillection.ui.item

import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.datum.DatumWrite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DatumFormFieldsTest {
    private val original = Datum(
        id = "d1",
        type = "text",
        label = "Author",
        value = "Tolkien",
        position = 1,
        currency = null,
        image = null,
        imageSmallThumbnail = null,
        imageLargeThumbnail = null,
        file = null,
        video = null,
        originalFilename = null
    )

    @Test
    fun validate_rejectsBlankLabel() {
        assertNotNull(DatumFormFields(label = " ", value = "x").validate())
    }

    @Test
    fun validate_rejectsNonNumberForNumberType() {
        assertNotNull(DatumFormFields(label = "Pages", type = "number", value = "abc").validate())
        assertNull(DatumFormFields(label = "Pages", type = "number", value = "12.5").validate())
    }

    @Test
    fun toCreate_buildsItemIri() {
        assertEquals(
            DatumWrite(item = "/api/items/i1", type = "text", label = "Author", value = "Tolkien", position = 3),
            DatumFormFields(label = "Author ", value = " Tolkien").toCreate("i1", position = 3)
        )
    }

    @Test
    fun toPatch_sendsOnlyChangedFields() {
        assertNull(DatumFormFields.from(original).toPatch(original))
        assertEquals(
            DatumWrite(value = "J.R.R. Tolkien"),
            DatumFormFields.from(original).copy(value = "J.R.R. Tolkien").toPatch(original)
        )
    }
}
