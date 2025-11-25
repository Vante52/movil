package com.example.fitmatch.model.delivery

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val timeWindow: String
)