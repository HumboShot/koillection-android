package com.humboshot.koillection.ui.wishlist

import com.humboshot.koillection.data.wishlist.Wish
import com.humboshot.koillection.data.wishlist.WishWrite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WishFormFieldsTest {
    private val original = Wish(
        id = "w1", name = "Artbook", url = "https://shop.example.com/a", price = "29.99",
        currency = "EUR", comment = "Signed", visibility = "public",
    )

    @Test
    fun validate_checksNameLinkPriceAndCurrency() {
        assertNotNull(WishFormFields(name = " ").validate())
        assertNotNull(WishFormFields(name = "A", url = "shop.example.com").validate())
        assertNotNull(WishFormFields(name = "A", price = "cheap").validate())
        assertNotNull(WishFormFields(name = "A", currency = "Euro").validate())
        assertNull(WishFormFields(name = "A", url = "HTTPS://shop.example.com/a", price = "12,50", currency = "eur").validate())
    }

    @Test
    fun toCreate_cleansValuesAndBuildsWishlistIri() {
        assertEquals(
            WishWrite(name = "Artbook", price = "12.50", currency = "EUR", wishlist = "/api/wishlists/l1", visibility = "public"),
            WishFormFields(name = "Artbook ", price = " 12,50", currency = "eur").toCreate("l1")
        )
    }

    @Test
    fun toPatch_sendsOnlyChanges() {
        val fields = WishFormFields.from(original)

        assertNull(fields.toPatch(original))
        assertEquals(WishWrite(price = "25"), fields.copy(price = "25").toPatch(original))
        assertEquals(WishWrite(comment = ""), fields.copy(comment = "").toPatch(original))
    }

    @Test
    fun clearsPriceOrCurrency_detectsCleared() {
        val fields = WishFormFields.from(original)

        assertFalse(fields.clearsPriceOrCurrency(original))
        assertTrue(fields.copy(price = "").clearsPriceOrCurrency(original))
        assertTrue(fields.copy(currency = " ").clearsPriceOrCurrency(original))
    }
}
