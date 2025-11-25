package com.example.fitmatch.model.product

import com.google.firebase.Timestamp

data class ProductInteraction(
    val id: String,
    val userId: String,                // Cliente
    val productId: String,
    val action: String,                // "LIKE" | "PASS" | "SAVE"
    val timestamp: Timestamp
)