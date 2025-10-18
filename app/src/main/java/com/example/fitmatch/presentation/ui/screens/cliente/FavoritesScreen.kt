package com.example.fitmatch.presentation.ui.screens.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.FitMatchTheme

/* ---------- Modelo de datos ---------- */
data class FavoriteProduct(
    val id: String,
    val category: String,
    val title: String,
    val subtitle: String? = null,
    val price: String,
    val inCartCount: Int = 0
)

/* ---------- Pantalla ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    filters: List<Pair<String, Int>> = listOf(
        "Todas" to 26, "Streetwear" to 8, "Oficina" to 5, "Evento" to 7
    ),
    products: List<FavoriteProduct> = sampleFavorites,
    onBack: () -> Unit = {},
    onAddCategory: () -> Unit = {},
    onOpenProduct: (FavoriteProduct) -> Unit = {},
    onCartClick: (FavoriteProduct) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    var selectedFilter by remember { mutableStateOf(filters.first().first) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            // Header unificado: título centrado + back + acción "+"
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
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Back
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.onSurface
                        )
                    }

                    // Título centrado (misma tipografía)
                    Text(
                        text = "Favoritos",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = colors.onSurface
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Acción: agregar categoría (+)
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onAddCategory) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Nueva categoría",
                                tint = colors.onSurface
                            )
                        }
                    }
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            /* Chips de filtros */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { (name, count) ->
                    FilterChip(
                        selected = selectedFilter == name,
                        onClick = { selectedFilter = name },
                        label = {
                            Text("$name $count", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    )
                }
            }

            /* Grid de tarjetas (2 columnas) */
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { p ->
                    FavoriteCard(
                        product = p,
                        onOpen = { onOpenProduct(p) },
                        onCart = { onCartClick(p) }
                    )
                }
            }
        }
    }
}

/* ---------- Tarjeta de producto ---------- */
@Composable
private fun FavoriteCard(
    product: FavoriteProduct,
    onOpen: () -> Unit,
    onCart: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    ElevatedCard( // sombra perceptible también en dark
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onOpen() }
    ) {
        Column(Modifier.padding(12.dp)) {

            /* Fila superior: “Prenda” (o categoría) + menú */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Prenda",
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Más opciones",
                    tint = colors.onSurfaceVariant
                )
            }

            /* Placeholder de carrusel / imagen */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(top = 8.dp, bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Carrusel\n(fotos / video)",
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 14.sp
                )
            }

            /* Categoría + Título + Precio */
            Text(
                text = product.category,
                fontSize = 12.sp,
                color = colors.onSurfaceVariant
            )
            Text(
                text = product.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.price,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Spacer(Modifier.weight(1f))

                /* Carrito con “badge” de cantidad */
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceContainerHigh)
                        .clickable { onCart() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Agregar al carrito",
                        tint = colors.onSurface
                    )

                    if (product.inCartCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(colors.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = product.inCartCount.toString(),
                                color = colors.onPrimary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Datos de ejemplo ---------- */
private val sampleFavorites = listOf(
    FavoriteProduct("1", "Oficina", "Blazer Sastre", price = "$189.900"),
    FavoriteProduct("2", "Streetwear", "Sneakers Eco", price = "$239.900", inCartCount = 2),
    FavoriteProduct("3", "Jeans", "Jeans Classic", price = "$129.900"),
    FavoriteProduct("4", "Zapatos", "Sandalias Midi", price = "$159.900"),
    FavoriteProduct("5", "Eventos", "Vestido Gala", price = "$329.900"),
    FavoriteProduct("6", "Streetwear", "Bolso Mini", price = "$159.900")
)

/* ---------- Previews con tu tema ---------- */
@Preview(showBackground = true, name = "Favoritos – Light")
@Composable
private fun FavoritesPreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        FavoritesScreen()
    }
}

@Preview(showBackground = true, name = "Favoritos – Dark")
@Composable
private fun FavoritesPreviewDark() {
    FitMatchTheme(darkTheme = true, dynamicColor = false) {
        FavoritesScreen()
    }
}
