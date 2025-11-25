package com.example.fitmatch.model.order

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class CartItem(
    val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val vendorId: String = "",
    val vendorName: String = "",
    val productTitle: String = "",
    val productImageUrl: String = "",
    val price: Int = 0,
    val quantity: Int = 0,
    val size: String = "",
    val color: String = "",
    val addedAt: Long = 0L
)