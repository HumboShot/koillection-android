package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.wishlist.Wish
import com.humboshot.koillection.data.wishlist.Wishlist
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WishlistService {
    @GET("/api/wishlists")
    suspend fun getWishlists(@Query("page") page: Int = 1): Result<HydraCollection<Wishlist>>

    @GET("/api/wishlists/{id}")
    suspend fun getWishlist(@Path("id") id: String): Result<Wishlist>

    @GET("/api/wishlists/{id}/children")
    suspend fun getChildren(@Path("id") id: String, @Query("page") page: Int = 1): Result<HydraCollection<Wishlist>>

    @GET("/api/wishlists/{id}/wishes")
    suspend fun getWishes(@Path("id") id: String, @Query("page") page: Int = 1): Result<HydraCollection<Wish>>
}
