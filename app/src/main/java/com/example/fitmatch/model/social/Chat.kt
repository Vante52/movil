package com.example.fitmatch.model.social

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Chat(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageAt: Long = 0L,
    val unreadCount: Map<String, Int> = emptyMap(), // userId -> count
    val isTito: Boolean = false
)
