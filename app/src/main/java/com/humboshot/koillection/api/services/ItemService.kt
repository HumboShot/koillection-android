package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.item.ItemWrite
import com.humboshot.koillection.data.tag.Tag
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

interface ItemService {
    @GET("/api/items/{id}")
    suspend fun getItem(@Path("id") id: String): Result<Item>

    @GET("/api/items/{id}/data")
    suspend fun getData(@Path("id") id: String, @Query("page") page: Int = 1): Result<HydraCollection<Datum>>

    @GET("/api/items/{id}/tags")
    suspend fun getTags(@Path("id") id: String, @Query("page") page: Int = 1): Result<HydraCollection<Tag>>

    @GET("/api/items/{id}/related_items")
    suspend fun getRelatedItems(@Path("id") id: String, @Query("page") page: Int = 1): Result<HydraCollection<Item>>

    @POST("/api/items")
    suspend fun createItem(@Body item: ItemWrite): Result<Item>

    @Headers("Content-Type: application/merge-patch+json")
    @PATCH("/api/items/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body item: ItemWrite): Result<Item>

    @DELETE("/api/items/{id}")
    suspend fun deleteItem(@Path("id") id: String): Result<Unit>

    @Multipart
    @POST("/api/items/{id}/image")
    suspend fun uploadImage(@Path("id") id: String, @Part file: MultipartBody.Part): Result<Item>
}
