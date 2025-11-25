package com.example.fitmatch.presentation.ui.screens.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitoChatScreen(
    onBackClick: () -> Unit = {},
    onViewProduct: () -> Unit = {},
    onViewProfile: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            // Header unificado: fondo surface, título centrado, back y acciones
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = colors.surface,
                tonalElevation = 1.dp,
                shadowElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Back
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }

                    // Título centrado (+ estado en línea como subtítulo pequeño)
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Tito",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp,
                                color = colors.onSurface
                            )
                        )
                        Text(
                            "Online",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.primary,
                            fontSize = 12.sp
                        )
                    }

                    // Acciones (más opciones)
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        IconButton(onClick = { /* TODO: abrir menú */ }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                        }
                    }
                }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Foto del usuario enviada (placeholder)
            RoundedImagePlaceholder(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(180.dp)
            )

            // Mensaje del usuario
            UserBubble("¿Sabes dónde puedo conseguir algo similar?")

            // Respuesta de Tito
            TitoBubble("Pues claro, ¡qué chimba de prenda! 😎")

            // Tarjeta de recomendación (como en el mock)
            RecommendationCard(
                title = "Camisa Grid Roja",
                handle = "@urbanx",
                price = "$129.900",
                rating = "4.7",
                onViewProduct = onViewProduct,
                onViewProfile = onViewProfile
            )

            Spacer(Modifier.weight(1f))

            // Input
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Escribe un mensaje…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                trailingIcon = {
                    IconButton(onClick = { /* enviar */ }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                    }
                }
            )
        }
    }
}

@Composable
private fun RoundedImagePlaceholder(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Image, contentDescription = null, tint = colors.onSurfaceVariant)
    }
}

@Composable
private fun UserBubble(text: String) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            color = colors.surface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp
        ) { Text(text, Modifier.padding(12.dp)) }
    }
}

@Composable
private fun TitoBubble(text: String) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth()) {
        Surface(
            color = colors.primary.copy(alpha = .10f),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp
        ) { Text(text, Modifier.padding(12.dp)) }
    }
}

@Composable
private fun RecommendationCard(
    title: String,
    handle: String,
    price: String,
    rating: String,
    onViewProduct: () -> Unit,
    onViewProfile: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RoundedImagePlaceholder(modifier = Modifier.size(44.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    Text(handle, color = colors.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(price, fontWeight = FontWeight.Bold)
            AssistChip(
                onClick = {},
                label = { Text("$rating ★") },
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "Creo que esta te quedará brutal 🔥",
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onViewProduct,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Ver producto") }
                OutlinedButton(
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Ver perfil") }
            }
        }
    }
}
