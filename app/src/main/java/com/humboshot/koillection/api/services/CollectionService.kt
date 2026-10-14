package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.collection.CollectionWrite
import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.item.Item
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

interface CollectionService {
    @GET("/api/collections")
    suspend fun getCollections(@Query("page") page: Int = 1): Result<HydraCollection<KCollection>>

    @GET("/api/collections/{id}")
    suspend fun getCollection(@Path("id") id: String): Result<KCollection>

    @GET("/api/collections/{id}/children")
    suspend fun getChildren(@Path("id") id: String, @Query("page") page: Int = 1): Result<HydraCollection<KCollection>>

    @GET("/api/collections/{id}/items")
    suspend fun getItems(@Path("id") id: String, @Query("page") page: Int = 1): Result<HydraCollection<Item>>

    @GET("/api/collections/{id}/data")
    suspend fun getData(@Path("id") id: String, @Query("page") page: Int = 1): Result<HydraCollection<Datum>>

    @POST("/api/collections")
    suspend fun createCollection(@Body collection: CollectionWrite): Result<KCollection>

    @Headers("Content-Type: application/merge-patch+json")
    @PATCH("/api/collections/{id}")
    suspend fun updateCollection(@Path("id") id: String, @Body collection: CollectionWrite): Result<KCollection>

    @DELETE("/api/collections/{id}")
    suspend fun deleteCollection(@Path("id") id: String): Result<Unit>

    @Multipart
    @POST("/api/collections/{id}/image")
    suspend fun uploadImage(@Path("id") id: String, @Part file: MultipartBody.Part): Result<KCollection>
}
