package com.humboshot.koillection.data.repository

import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.UserService
import com.humboshot.koillection.auth.AuthStore
import com.humboshot.koillection.data.user.User
import com.humboshot.koillection.data.user.currentUser

object UserRepository {
    private val service: UserService
        get() = ApiClient.getInstance().create(UserService::class.java)

    suspend fun getCurrentUser(): Result<User> =
        getAllPages { page -> service.getUsers(page) }
            .mapCatching { users -> checkNotNull(users.currentUser(AuthStore.username)) { "User not found" } }
}
