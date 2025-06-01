package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.collection.Collection
import retrofit2.Response
import retrofit2.http.GET

interface CollectionService {
    @GET("/api/collections")
    suspend fun getCollections(): Response<Collection>
}