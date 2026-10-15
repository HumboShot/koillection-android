package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.data.tag.TagCategory
import com.humboshot.koillection.data.tag.TagWrite
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

interface TagService {
    @GET("/api/tags")
    suspend fun getTags(@Query("page") page: Int = 1): Result<HydraCollection<Tag>>

    @GET("/api/tags/{id}")
    suspend fun getTag(@Path("id") id: String): Result<Tag>

    @GET("/api/tags/{id}/items")
    suspend fun getTagItems(@Path("id") id: String, @Query("page") page: Int = 1): Result<HydraCollection<Item>>

    @POST("/api/tags")
    suspend fun createTag(@Body tag: TagWrite): Result<Tag>

    @Headers("Content-Type: application/merge-patch+json")
    @PATCH("/api/tags/{id}")
    suspend fun updateTag(@Path("id") id: String, @Body tag: TagWrite): Result<Tag>

    @DELETE("/api/tags/{id}")
    suspend fun deleteTag(@Path("id") id: String): Result<Unit>

    @Multipart
    @POST("/api/tags/{id}/image")
    suspend fun uploadImage(@Path("id") id: String, @Part file: MultipartBody.Part): Result<Tag>

    @GET("/api/tag_categories")
    suspend fun getCategories(@Query("page") page: Int = 1): Result<HydraCollection<TagCategory>>

    @GET("/api/tag_categories/{id}")
    suspend fun getCategory(@Path("id") id: String): Result<TagCategory>
}
