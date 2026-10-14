package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.tag.Tag
import retrofit2.http.GET
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
}
