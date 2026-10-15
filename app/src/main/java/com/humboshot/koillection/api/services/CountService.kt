package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.hydra.HydraTotal
import retrofit2.http.GET
import retrofit2.http.Path

/** Reads only `totalItems` from the first page of a list. */
interface CountService {
    @GET("/api/{resource}")
    suspend fun count(@Path("resource") resource: String): Result<HydraTotal>
}
