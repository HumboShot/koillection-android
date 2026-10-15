package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.log.Log
import retrofit2.http.GET
import retrofit2.http.Query

interface LogService {
    @GET("/api/logs")
    suspend fun getLogs(@Query("page") page: Int = 1): Result<HydraCollection<Log>>
}
