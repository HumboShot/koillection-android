package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.item.Item
import retrofit2.http.GET
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
}
