package com.humboshot.koillection.ui.collection

import com.humboshot.koillection.data.collection.CollectionWrite
import com.humboshot.koillection.data.collection.KCollection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CollectionFormFieldsTest {
    private val original = KCollection(
        color = "#000000",
        createdAt = "2026-01-01T00:00:00+00:00",
        finalVisibility = "public",
        id = "c1",
        image = null,
        itemsDefaultTemplate = null,
        owner = null,
        parent = null,
        parentVisibility = null,
        scrapedFromUrl = null,
        seenCounter = 0,
        title = "Books",
        updatedAt = null,
        visibility = "public"
    )

    @Test
    fun validate_rejectsBlankTitle() {
        assertNotNull(CollectionFormFields(title = " ").validate())
    }

    @Test
    fun toCreate_topLevelHasNoParent() {
        assertEquals(
            CollectionWrite(title = "Books", visibility = "public"),
            CollectionFormFields(title = "Books ").toCreate(parentId = null)
        )
    }

    @Test
    fun toCreate_childHasParentIri() {
        assertEquals("/api/collections/p1", CollectionFormFields(title = "Books").toCreate("p1").parent)
    }

    @Test
    fun toPatch_returnsNullWhenNothingChanged() {
        assertNull(CollectionFormFields.from(original).toPatch(original))
    }

    @Test
    fun toPatch_sendsOnlyChangedFields() {
        assertEquals(
            CollectionWrite(visibility = "private"),
            CollectionFormFields.from(original).copy(visibility = "private").toPatch(original)
        )
    }
}
