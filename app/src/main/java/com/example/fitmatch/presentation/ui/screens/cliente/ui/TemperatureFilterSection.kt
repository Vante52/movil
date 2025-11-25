package com.example.fitmatch.presentation.ui.screens.cliente.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitmatch.presentation.viewmodel.user.TemperatureViewModel

@Composable
fun TemperatureFilterSection(
    viewModel: TemperatureViewModel,
    onFilterChange: (String) -> Unit
) {
    val temperature by viewModel.temperature.collectAsState()
    val suggestion by viewModel.clothingSuggestion.collectAsState()

    LaunchedEffect(temperature) {
        if (temperature != null) {
            onFilterChange(suggestion)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Temperatura actual: ${temperature?.let { "$it °C" } ?: "No disponible"}")
        Text("Sugerencia: $suggestion")
    }
}