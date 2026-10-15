package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.wishlist.Wish
import com.humboshot.koillection.data.wishlist.Wishlist
import com.humboshot.koillection.data.wishlist.WishlistWrite
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
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

    @POST("/api/wishlists")
    suspend fun createWishlist(@Body wishlist: WishlistWrite): Result<Wishlist>

    @Headers("Content-Type: application/merge-patch+json")
    @PATCH("/api/wishlists/{id}")
    suspend fun updateWishlist(@Path("id") id: String, @Body wishlist: WishlistWrite): Result<Wishlist>

    @DELETE("/api/wishlists/{id}")
    suspend fun deleteWishlist(@Path("id") id: String): Result<Unit>

    @Multipart
    @POST("/api/wishlists/{id}/image")
    suspend fun uploadImage(@Path("id") id: String, @Part file: MultipartBody.Part): Result<Wishlist>
}
