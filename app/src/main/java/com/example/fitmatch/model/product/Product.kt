package com.example.fitmatch.model.product

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Product(
    val id: String = "",
    val vendorId: String = "",              // Referencia a User
    val title: String = "",
    val description: String = "",
    val price: Int = 0,
    val originalPrice: Int? = null,           // Para descuentos
    val brand: String = "",
    val category: String = "",
    val condition: String = "",
    val color: String = "",
    val sizes: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),       // URLs de Storage
    val stock: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)