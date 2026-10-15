package com.humboshot.koillection.ui.tag

import com.humboshot.koillection.data.tag.TagCategory
import com.humboshot.koillection.data.tag.TagCategoryWrite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TagCategoryFormFieldsTest {
    private val original = TagCategory(id = "c1", label = "Genre", description = null, color = "#009688")

    @Test
    fun validate_needsLabelAndValidColor() {
        assertNotNull(TagCategoryFormFields(label = " ").validate())
        assertNotNull(TagCategoryFormFields(label = "Genre", color = "teal").validate())
        assertNull(TagCategoryFormFields(label = "Genre", color = "").validate())
        assertNull(TagCategoryFormFields(label = "Genre", color = "00AA88").validate())
    }

    @Test
    fun toCreate_normalizesColorAndSkipsEmptyValues() {
        assertEquals(
            TagCategoryWrite(label = "Genre", color = "#00aa88"),
            TagCategoryFormFields(label = "Genre", description = " ", color = "00AA88").toCreate()
        )
        assertEquals(TagCategoryWrite(label = "Genre"), TagCategoryFormFields(label = "Genre").toCreate())
    }

    @Test
    fun toPatch_sendsOnlyChanges() {
        val fields = TagCategoryFormFields.from(original)

        assertNull(fields.toPatch(original))
        assertNull(fields.copy(color = "#009688 ").toPatch(original))
        assertEquals(TagCategoryWrite(color = "#e74646"), fields.copy(color = "#E74646").toPatch(original))
        assertEquals(TagCategoryWrite(description = "Kind of story"), fields.copy(description = "Kind of story").toPatch(original))
    }
}
