package com.humboshot.koillection.ui.wishlist

import com.humboshot.koillection.data.wishlist.Wishlist
import com.humboshot.koillection.data.wishlist.WishlistWrite
import com.humboshot.koillection.extension.toIri

data class WishlistFormFields(
    val name: String = "",
    val visibility: String = "public",
) {
    fun validate(): String? = if (name.isBlank()) "Name is required" else null

    fun toCreate(parentId: String?): WishlistWrite = WishlistWrite(
        name = name.trim(),
        parent = parentId?.toIri("wishlists"),
        visibility = visibility,
    )

    /** Gives only the fields that differ from [original], or null when nothing changed. */
    fun toPatch(original: Wishlist): WishlistWrite? {
        val patch = WishlistWrite(
            name = name.trim().takeIf { it != original.name },
            visibility = visibility.takeIf { it != original.visibility },
        )
        return patch.takeIf { it != WishlistWrite() }
    }

    companion object {
        fun from(wishlist: Wishlist) = WishlistFormFields(
            name = wishlist.name,
            visibility = wishlist.visibility ?: "public",
        )
    }
}
