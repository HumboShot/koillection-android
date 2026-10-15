package com.humboshot.koillection.ui.item

import com.humboshot.koillection.data.item.Item
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

    private val original = Item(
        id = "i1",
        name = "Book",
        quantity = 1,
        collection = "/api/collections/c1",
        image = null,
        imageSmallThumbnail = null,
        imageLargeThumbnail = null,
        visibility = "public",
        createdAt = "2026-01-01T00:00:00+00:00",
        updatedAt = null
    )

    @Test
    fun toPatch_returnsNullWhenNothingChanged() {
        assertNull(ItemFormFields.from(original).toPatch(original))
    }

    @Test
    fun toPatch_sendsOnlyChangedFields() {
        val fields = ItemFormFields.from(original).copy(name = "Book 2", visibility = "private")

        assertEquals(ItemWrite(name = "Book 2", visibility = "private"), fields.toPatch(original))
    }

    @Test
    fun toPatch_ignoresWhitespaceOnlyNameChange() {
        val fields = ItemFormFields.from(original).copy(name = "Book ", quantity = "2")

        assertEquals(ItemWrite(quantity = 2), fields.toPatch(original))
    }

    @Test
    fun toPatch_sendsFullTagListOnlyWhenTagsChanged() {
        val fields = ItemFormFields.from(original, tagIds = listOf("t1", "t2"))

        assertNull(fields.toPatch(original, originalTagIds = listOf("t2", "t1")))
        assertEquals(
            ItemWrite(tags = listOf("/api/tags/t1", "/api/tags/t2", "/api/tags/t3")),
            fields.addTag("t3").toPatch(original, originalTagIds = listOf("t1", "t2"))
        )
        assertEquals(
            ItemWrite(tags = emptyList()),
            fields.removeTag("t1").removeTag("t2").toPatch(original, originalTagIds = listOf("t1", "t2"))
        )
    }

    @Test
    fun addTag_ignoresDuplicates() {
        assertEquals(listOf("t1"), ItemFormFields(name = "Book").addTag("t1").addTag("t1").tagIds)
    }

    @Test
    fun toCreate_sendsTagsOnlyWhenThereAreSome() {
        assertNull(ItemFormFields(name = "Book").toCreate("c1").tags)
        assertEquals(listOf("/api/tags/t1"), ItemFormFields(name = "Book", tagIds = listOf("t1")).toCreate("c1").tags)
    }
}
