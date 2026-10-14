package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.datum.DatumWrite
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface DatumService {
    @POST("/api/data")
    suspend fun createDatum(@Body datum: DatumWrite): Result<Datum>

    @Headers("Content-Type: application/merge-patch+json")
    @PATCH("/api/data/{id}")
    suspend fun updateDatum(@Path("id") id: String, @Body datum: DatumWrite): Result<Datum>

    @DELETE("/api/data/{id}")
    suspend fun deleteDatum(@Path("id") id: String): Result<Unit>
}
