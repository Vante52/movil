package com.example.fitmatch.presentation.ui.screens.auth.domain.repository

interface AuthRepository{
    suspend fun  login (login: String, password: String): Result<Unit>
}