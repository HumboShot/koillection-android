package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.data.tag.TagCategory
import retrofit2.http.GET
import retrofit2.http.Query

interface TagService {
    @GET("/api/tags")
    suspend fun getTags(@Query("page") page: Int = 1): Result<HydraCollection<Tag>>

    @GET("/api/tag_categories")
    suspend fun getCategories(@Query("page") page: Int = 1): Result<HydraCollection<TagCategory>>
}
