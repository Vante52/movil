package com.example.fitmatch.model.delivery

import com.google.firebase.Timestamp

import android.location.Location

data class Delivery(
    val id: String,
    val orderId: String,
    val driverId: String?,
    val pickupLocation: Location,
    val deliveryLocation: Location,
    val currentStep: Int,
    val status: String,
    val estimatedTime: String,
    val createdAt: Timestamp
)
