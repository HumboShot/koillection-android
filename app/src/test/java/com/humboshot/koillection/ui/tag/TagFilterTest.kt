package com.humboshot.koillection.ui.tag

import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.data.tag.TagCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class TagFilterTest {
    private fun tag(id: String, label: String, category: String? = null) =
        Tag(id = id, label = label, description = null, imageSmallThumbnail = null, category = category)

    private val fantasy = tag("1", "Fantasy", category = "/api/tag_categories/c1")
    private val manga = tag("2", "Manga")
    private val categories = mapOf("c1" to TagCategory(id = "c1", label = "Genre"))

    @Test
    fun blankQuery_keepsAll() {
        assertEquals(listOf(fantasy, manga), filterTags(listOf(fantasy, manga), "  "))
    }

    @Test
    fun matchesLabelIgnoringCase() {
        assertEquals(listOf(manga), filterTags(listOf(fantasy, manga), "MAN"))
    }

    @Test
    fun matchesCategoryLabel() {
        assertEquals(listOf(fantasy), filterTags(listOf(fantasy, manga), "genre", categories))
    }
}
