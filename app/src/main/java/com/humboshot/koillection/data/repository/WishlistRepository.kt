package com.humboshot.koillection.data.repository

import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.WishlistService
import com.humboshot.koillection.data.wishlist.Wish
import com.humboshot.koillection.data.wishlist.Wishlist

object WishlistRepository {
    private val service: WishlistService
        get() = ApiClient.getInstance().create(WishlistService::class.java)

    /** `/api/wishlists` returns every wishlist, so keep only the ones without a parent. */
    suspend fun getRootWishlists(): Result<List<Wishlist>> =
        getAllPages { page -> service.getWishlists(page) }
            .map { wishlists -> wishlists.filter { it.parent == null }.sortedBy { it.name.lowercase() } }

    suspend fun getWishlist(id: String): Result<Wishlist> = service.getWishlist(id)

    suspend fun getChildren(id: String): Result<List<Wishlist>> =
        getAllPages { page -> service.getChildren(id, page) }
            .map { wishlists -> wishlists.sortedBy { it.name.lowercase() } }

    suspend fun getWishes(id: String): Result<List<Wish>> =
        getAllPages { page -> service.getWishes(id, page) }
            .map { wishes -> wishes.sortedBy { it.name.lowercase() } }
}
