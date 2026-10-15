package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.template.Field
import com.humboshot.koillection.data.template.FieldWrite
import com.humboshot.koillection.data.template.Template
import com.humboshot.koillection.data.template.TemplateWrite
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TemplateService {
    @GET("/api/templates")
    suspend fun getTemplates(@Query("page") page: Int = 1): Result<HydraCollection<Template>>

    @GET("/api/templates/{id}")
    suspend fun getTemplate(@Path("id") id: String): Result<Template>

    @GET("/api/templates/{id}/fields")
    suspend fun getFields(@Path("id") templateId: String, @Query("page") page: Int = 1): Result<HydraCollection<Field>>

    @POST("/api/templates")
    suspend fun createTemplate(@Body template: TemplateWrite): Result<Template>

    @Headers("Content-Type: application/merge-patch+json")
    @PATCH("/api/templates/{id}")
    suspend fun updateTemplate(@Path("id") id: String, @Body template: TemplateWrite): Result<Template>

    @DELETE("/api/templates/{id}")
    suspend fun deleteTemplate(@Path("id") id: String): Result<Unit>

    @POST("/api/fields")
    suspend fun createField(@Body field: FieldWrite): Result<Field>

    @Headers("Content-Type: application/merge-patch+json")
    @PATCH("/api/fields/{id}")
    suspend fun updateField(@Path("id") id: String, @Body field: FieldWrite): Result<Field>

    @DELETE("/api/fields/{id}")
    suspend fun deleteField(@Path("id") id: String): Result<Unit>
}
