package com.example.fitmatch.model.social

import com.google.firebase.Timestamp

data class Comment(
    val id: String,
    val productId: String,
    val userId: String,
    val text: String,
    val createdAt: Timestamp
)
