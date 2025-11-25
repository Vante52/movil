package com.example.fitmatch.model.product

import com.google.firebase.Timestamp

data class Product(
    val id: String,
    val vendorId: String,              // Referencia a User
    val title: String,
    val description: String,
    val price: Int,
    val originalPrice: Int?,           // Para descuentos
    val brand: String,
    val category: String,
    val condition: String,
    val color: String,
    val sizes: List<String>,
    val tags: List<String>,
    val imageUrls: List<String>,       // URLs de Storage
    val stock: Int,
    val isActive: Boolean,
    val createdAt: Timestamp,
    val updatedAt: Timestamp
)
