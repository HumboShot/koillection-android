package com.humboshot.koillection.ui.tag

import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.data.tag.TagWrite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TagFormFieldsTest {
    private val original = Tag(
        id = "t1", label = "Fantasy", description = "Dragons", imageSmallThumbnail = null,
        category = "/api/tag_categories/c1", visibility = "public",
    )

    @Test
    fun validate_needsLabel() {
        assertNotNull(TagFormFields(label = " ").validate())
        assertNull(TagFormFields(label = "Fantasy").validate())
    }

    @Test
    fun toCreate_buildsCategoryIriAndSkipsEmptyDescription() {
        assertEquals(
            TagWrite(label = "Fantasy", category = "/api/tag_categories/c1", visibility = "public"),
            TagFormFields(label = "Fantasy ", description = "  ", categoryId = "c1").toCreate()
        )
    }

    @Test
    fun toPatch_sendsOnlyChanges() {
        val fields = TagFormFields.from(original)

        assertNull(fields.toPatch(original))
        assertEquals(TagWrite(category = "/api/tag_categories/c2"), fields.copy(categoryId = "c2").toPatch(original))
        assertEquals(TagWrite(description = ""), fields.copy(description = "").toPatch(original))
    }
}
