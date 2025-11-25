package com.example.fitmatch.model.social

import com.google.firebase.Timestamp
data class Chat(
    val id: String,
    val participantIds: List<String>,
    val lastMessage: String,
    val lastMessageAt: Timestamp,
    val unreadCount: Map<String, Int>, // userId -> count
    val isTito: Boolean = false
)
