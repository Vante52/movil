package com.example.fitmatch.model.social

import com.google.firebase.Timestamp

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val text: String,
    val imageUrl: String?,
    val timestamp: Timestamp,
    val isRead: Boolean
)
