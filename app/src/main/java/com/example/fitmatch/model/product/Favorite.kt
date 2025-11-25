package com.example.fitmatch.model.product

import com.google.firebase.Timestamp

data class Favorite(
    val id: String,
    val userId: String,
    val productId: String,
    val category: String,              // Para filtros
    val addedAt: Timestamp
)