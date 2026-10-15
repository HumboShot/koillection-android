package com.humboshot.koillection.ui.wishlist

import com.humboshot.koillection.data.wishlist.Wish
import com.humboshot.koillection.data.wishlist.WishWrite
import com.humboshot.koillection.extension.toIri

private val URL = Regex("^https?://\\S+$", RegexOption.IGNORE_CASE)
private val CURRENCY = Regex("^[A-Za-z]{3}$")

data class WishFormFields(
    val name: String = "",
    val url: String = "",
    val price: String = "",
    /** A 3-letter currency code such as EUR. */
    val currency: String = "",
    val comment: String = "",
    val visibility: String = "public",
) {
    fun validate(): String? = when {
        name.isBlank() -> "Name is required"
        url.isNotBlank() && !URL.matches(url.trim()) -> "The link must start with http:// or https://"
        price.isNotBlank() && price.trim().replace(',', '.').toBigDecimalOrNull() == null -> "Price must be a number"
        currency.isNotBlank() && !CURRENCY.matches(currency.trim()) -> "Currency must be a 3-letter code, such as EUR"
        else -> null
    }

    private val cleanPrice: String? get() = price.trim().replace(',', '.').ifEmpty { null }
    private val cleanCurrency: String? get() = currency.trim().uppercase().ifEmpty { null }

    fun toCreate(wishlistId: String): WishWrite = WishWrite(
        name = name.trim(),
        url = url.trim().ifEmpty { null },
        price = cleanPrice,
        currency = cleanCurrency,
        wishlist = wishlistId.toIri("wishlists"),
        comment = comment.trim().ifEmpty { null },
        visibility = visibility,
    )

    /**
     * Gives only the fields that differ from [original], or null when nothing changed.
     * A cleared link or comment is sent as "". A price or currency cannot be cleared, because null is
     * left out of the JSON. [clearsPriceOrCurrency] tells the form to warn about that.
     */
    fun toPatch(original: Wish): WishWrite? {
        val patch = WishWrite(
            name = name.trim().takeIf { it != original.name },
            url = url.trim().takeIf { it != original.url.orEmpty() },
            price = cleanPrice?.takeIf { it != original.price },
            currency = cleanCurrency?.takeIf { it != original.currency },
            comment = comment.trim().takeIf { it != original.comment.orEmpty() },
            visibility = visibility.takeIf { it != original.visibility },
        )
        return patch.takeIf { it != WishWrite() }
    }

    fun clearsPriceOrCurrency(original: Wish): Boolean =
        (cleanPrice == null && !original.price.isNullOrBlank()) || (cleanCurrency == null && !original.currency.isNullOrBlank())

    companion object {
        fun from(wish: Wish) = WishFormFields(
            name = wish.name,
            url = wish.url.orEmpty(),
            price = wish.price.orEmpty(),
            currency = wish.currency.orEmpty(),
            comment = wish.comment.orEmpty(),
            visibility = wish.visibility ?: "public",
        )
    }
}
