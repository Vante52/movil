package com.example.fitmatch.model.social

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Notification(
    val id: String = "",
    val userId: String = "",
    val type: String = "",                  // "MATCH" | "ORDER" | "MESSAGE" | "CONFIRMATION"
    val title: String = "",
    val message: String = "",
    val productId: String? = null,
    val orderId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)