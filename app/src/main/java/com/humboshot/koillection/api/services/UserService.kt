package com.humboshot.koillection.api.services

import com.humboshot.koillection.data.hydra.HydraCollection
import com.humboshot.koillection.data.user.User
import retrofit2.http.GET
import retrofit2.http.Query

interface UserService {
    @GET("/api/users")
    suspend fun getUsers(@Query("page") page: Int = 1): Result<HydraCollection<User>>
}
