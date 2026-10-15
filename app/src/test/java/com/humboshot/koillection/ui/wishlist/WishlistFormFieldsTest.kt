package com.humboshot.koillection.ui.wishlist

import com.humboshot.koillection.data.wishlist.Wishlist
import com.humboshot.koillection.data.wishlist.WishlistWrite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WishlistFormFieldsTest {
    private val original = Wishlist(id = "w1", name = "Books", visibility = "public")

    @Test
    fun validate_needsName() {
        assertNotNull(WishlistFormFields(name = " ").validate())
        assertNull(WishlistFormFields(name = "Books").validate())
    }

    @Test
    fun toCreate_setsParentIriOnlyForSubWishlists() {
        assertEquals(WishlistWrite(name = "Books", visibility = "public"), WishlistFormFields(name = "Books ").toCreate(null))
        assertEquals("/api/wishlists/p1", WishlistFormFields(name = "Manga").toCreate("p1").parent)
    }

    @Test
    fun toPatch_sendsOnlyChanges() {
        val fields = WishlistFormFields.from(original)

        assertNull(fields.toPatch(original))
        assertEquals(WishlistWrite(visibility = "private"), fields.copy(visibility = "private").toPatch(original))
    }
}
