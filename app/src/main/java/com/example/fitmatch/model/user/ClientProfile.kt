package com.example.fitmatch.model.user
import kotlinx.serialization.Serializable

@Serializable
data class ClientProfile(
    val userId: String,
    val preferredSizes: List<String> = emptyList(),
    val preferredStyles: List<String> = emptyList(),
    val preferredColors: List<String> = emptyList(),
    val preferredCategories: List<String> = emptyList(),
    val language: String = "Español"
)