package com.example.fitmatch.presentation.ui.screens.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.FitMatchTheme

data class StoreProfile(
    val name: String,
    val followers: String,
    val following: String,
    val rating: String,
    val description: String,
    val hasNewPublications: Boolean,
    val mascotMessage: String
)

data class StoreProduct(
    val id: String,
    val name: String,
    val price: String,
    val originalPrice: String? = null,
    val likes: Int = 0,
    val comments: Int = 0,
    val hasDiscount: Boolean = false
)

// ⚠️ Sin @Preview aquí
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreProfileScreen(
    onBackClick: () -> Unit = {},
    onFollowClick: () -> Unit = {},
    onProductClick: (String) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    val storeProfile = remember {
        StoreProfile(
            name = "Atelier Nova",
            followers = "128",
            following = "12.4k",
            rating = "4.8★",
            description = "Moda sostenible de lujo a precio de Bogotá. Envíos a todo el país. Óptica rápida.",
            hasNewPublications = true,
            mascotMessage = "Tito ha confirmado las publicaciones ¡Excelente en tus gustos! Échale un vistazo."
        )
    }

    val products = remember {
        listOf(
            StoreProduct("1", "Blazer Premium en Lana", "$199,000", "$259,000", 24, 3, true),
            StoreProduct("2", "Blazer Premium en Lana", "$199,000", null, 18, 1),
            StoreProduct("3", "Blazer Premium en Lana", "$199,000", null, 31, 5),
            StoreProduct("4", "Blazer Premium en Lana", "$199,000", null, 12, 2)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        item {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.onSurface
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ubicación",
                            tint = colors.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat",
                            tint = colors.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        // Mejor como OutlinedButton con tokens del tema
                        OutlinedButton(
                            onClick = onFollowClick,
                            shape = RoundedCornerShape(20.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                width = 1.dp
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.onSurface
                            ),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Seguir",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        item {
            // Perfil de la tienda
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Avatar y stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = "Avatar tienda",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = storeProfile.followers,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "Seguidores",
                                fontSize = 12.sp,
                                color = colors.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = storeProfile.following,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "Siguiendo",
                                fontSize = 12.sp,
                                color = colors.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = storeProfile.rating,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "Rating",
                                fontSize = 12.sp,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nombre de la tienda
                Text(
                    text = storeProfile.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    text = storeProfile.description,
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mensaje de la mascota con notificación
                if (storeProfile.hasNewPublications) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar mascota
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🐶",
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = storeProfile.mascotMessage,
                                fontSize = 12.sp,
                                color = colors.onPrimaryContainer,
                                modifier = Modifier.weight(1f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            // Título Publicaciones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Publicaciones",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }
        }

        item {
            // Grid de productos
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(600.dp), // Altura fija para el grid (scroll dentro de la pantalla)
                userScrollEnabled = false
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun ProductCard(
    product: StoreProduct,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Imagen placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Imagen del producto",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )

                // Badge de descuento si aplica
                if (product.hasDiscount) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                colors.error,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OFERTA",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onError
                        )
                    }
                }
            }

            // Información del producto
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Precio
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.price,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )

                    product.originalPrice?.let {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = it,
                            fontSize = 10.sp,
                            color = colors.onSurfaceVariant,
                            style = LocalTextStyle.current.copy(
                                textDecoration = TextDecoration.LineThrough
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Likes y comentarios
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Likes",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = product.likes.toString(),
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comentarios",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = product.comments.toString(),
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// Previews con el FitMatchTheme

@Preview(showBackground = true, name = "Store Profile – Light (Brand)")
@Composable
private fun StoreProfilePreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        StoreProfileScreen()
    }
}

@Preview(showBackground = true, name = "Store Profile – Dark (Brand)")
@Composable
private fun StoreProfilePreviewDark() {
    FitMatchTheme(darkTheme = true, dynamicColor = false) {
        StoreProfileScreen()
    }
}
