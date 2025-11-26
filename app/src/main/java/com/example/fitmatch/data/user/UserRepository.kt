package com.example.fitmatch.data.user

import com.example.fitmatch.model.user.User

interface UserRepository {
    suspend fun searchUsers(query: String, excludeUserId: String? = null): List<User>
    suspend fun getAllUsers(excludeUserId: String? = null): List<User>
}
