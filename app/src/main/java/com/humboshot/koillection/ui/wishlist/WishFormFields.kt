package com.humboshot.koillection.ui.wishlist

import com.humboshot.koillection.data.wishlist.Wish
import com.humboshot.koillection.data.wishlist.WishWrite
import com.humboshot.koillection.extension.toIri

private val URL = Regex("^https?://\\S+$", RegexOption.IGNORE_CASE)
private val PRICE = Regex("^\\d+([.,]\\d{1,2})?$")

/** True for a real ISO 4217 code such as EUR. */
private fun isCurrencyCode(code: String): Boolean =
    code.length == 3 && runCatching { java.util.Currency.getInstance(code.uppercase()) }.isSuccess

/**
 * The link to open in the browser, or null when it cannot be opened. A link without a scheme, such as
 * `www.shop.com`, gets `https://`. Other schemes (for example `javascript:`) are not opened.
 */
fun openableLink(url: String?): String? {
    val link = url?.trim().orEmpty()
    if (link.isEmpty()) return null
    if (URL.matches(link)) return link
    return if (':' !in link && ' ' !in link) "https://$link" else null
}

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
        price.isNotBlank() && !PRICE.matches(price.trim()) -> "Price must be a number, such as 12.50"
        currency.isNotBlank() && !isCurrencyCode(currency.trim()) -> "Currency must be a currency code, such as EUR"
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
