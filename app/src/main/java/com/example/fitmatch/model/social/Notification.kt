package com.example.fitmatch.model.social

import com.google.firebase.Timestamp

data class Notification(
    val id: String,
    val userId: String,
    val type: String,                  // "MATCH" | "ORDER" | "MESSAGE" | "CONFIRMATION"
    val title: String,
    val message: String,
    val productId: String?,
    val orderId: String?,
    val isRead: Boolean,
    val createdAt: Timestamp
)