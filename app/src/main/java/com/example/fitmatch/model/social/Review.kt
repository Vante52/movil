package com.example.fitmatch.model.social

import com.google.firebase.Timestamp

data class Review(
    val id: String,
    val orderId: String,
    val reviewerId: String,
    val reviewedId: String,            // vendorId o buyerId
    val rating: Float,
    val comment: String,
    val createdAt: Timestamp
)
