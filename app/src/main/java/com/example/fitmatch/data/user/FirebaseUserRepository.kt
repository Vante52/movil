package com.example.fitmatch.data.user

import com.example.fitmatch.model.user.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {

    override suspend fun searchUsers(query: String, excludeUserId: String?): List<User> {
        val normalizedQuery = query.trim()

        val snapshot = firestore.collection("users")
            .get()
            .await()

        return snapshot.documents
            .mapNotNull { it.toObject(User::class.java) }
            .filter { user -> excludeUserId == null || user.id != excludeUserId }
            .filter { user ->
                if (normalizedQuery.isBlank()) true
                else user.fullName.contains(normalizedQuery, ignoreCase = true) ||
                    user.email.contains(normalizedQuery, ignoreCase = true)
            }
    }

    override suspend fun getAllUsers(excludeUserId: String?): List<User> {
        return searchUsers(query = "", excludeUserId = excludeUserId)
    }
}
