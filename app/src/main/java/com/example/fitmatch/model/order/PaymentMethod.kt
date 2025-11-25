package com.example.fitmatch.model.order

data class PaymentMethod(
    val id: String,
    val userId: String,
    val type: String,
    val lastFourDigits: String
)
