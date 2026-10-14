package com.humboshot.koillection.ui.item

import com.humboshot.koillection.data.item.ItemWrite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ItemFormFieldsTest {
    @Test
    fun validate_rejectsBlankName() {
        assertNotNull(ItemFormFields(name = "  ").validate())
    }

    @Test
    fun validate_rejectsZeroOrEmptyQuantity() {
        assertNotNull(ItemFormFields(name = "Book", quantity = "0").validate())
        assertNotNull(ItemFormFields(name = "Book", quantity = "").validate())
    }

    @Test
    fun validate_acceptsValidFields() {
        assertNull(ItemFormFields(name = "Book", quantity = "3").validate())
    }

    @Test
    fun toCreate_trimsNameAndBuildsCollectionIri() {
        assertEquals(
            ItemWrite(name = "Book", quantity = 3, collection = "/api/collections/c1", visibility = "private"),
            ItemFormFields(name = " Book ", quantity = "3", visibility = "private").toCreate("c1")
        )
    }
}
