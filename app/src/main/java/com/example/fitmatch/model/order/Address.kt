package com.example.fitmatch.model.order

data class Address(
    val id: String,
    val userId: String,
    val label: String,
    val street: String,
    val city: String,
    val country: String,
    val isPrimary: Boolean
)
