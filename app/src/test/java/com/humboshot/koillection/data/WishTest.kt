package com.humboshot.koillection.data

import com.humboshot.koillection.data.wishlist.Wish
import org.junit.Assert.assertEquals
import org.junit.Test

class WishTest {
    private val wish = Wish(id = "w1", name = "Artbook")

    @Test
    fun priceText_showsPriceAndCurrency() {
        assertEquals("12.50 EUR", wish.copy(price = "12.50", currency = "EUR").priceText)
        assertEquals("12.50", wish.copy(price = "12.50").priceText)
    }

    @Test
    fun priceText_isEmptyWithoutPrice() {
        assertEquals("", wish.copy(currency = "EUR").priceText)
        assertEquals("", wish.copy(price = " ", currency = "EUR").priceText)
    }
}
