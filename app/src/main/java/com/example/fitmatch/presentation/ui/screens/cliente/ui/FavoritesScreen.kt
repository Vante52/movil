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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.FitMatchTheme
import com.example.fitmatch.presentation.ui.screens.cliente.state.FavoriteProductState
import com.example.fitmatch.presentation.viewmodel.user.FavoritesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBack: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onAddCategory: () -> Unit = {},
    onOpenProduct: (String) -> Unit = {},
    viewModel: FavoritesViewModel = viewModel()
) {
    val colors = MaterialTheme.colorScheme

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surface,
                tonalElevation = 1.dp,
                shadowElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
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

                    Text(
                        text = "Favoritos",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = colors.onSurface
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )

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
        if (uiState.isLoading) {
            // Estado de carga
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.isEmpty) {
            // Estado vacío
            EmptyFavoritesState(
                onBack = onBack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
            )
        } else {
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
                    uiState.filters.forEach { filter ->
                        FilterChip(
                            selected = uiState.selectedFilter == filter.name,
                            onClick = { viewModel.onFilterSelected(filter.name) },
                            label = {
                                Text(
                                    "${filter.name} ${filter.count}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
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
                    items(uiState.filteredProducts, key = { it.id }) { product ->
                        FavoriteCard(
                            product = product,
                            onOpen = { onOpenProduct(product.id) },
                            onCart = { viewModel.onAddToCart(product.id) }
                        )
                    }
                }
            }
        }
    }
}

//composables
@Composable
private fun EmptyFavoritesState(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❤️",
            fontSize = 64.sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No tienes favoritos aún",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Guarda productos para verlos aquí",
            color = colors.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Explorar productos")
        }
    }
}

@Composable
private fun FavoriteCard(
    product: FavoriteProductState,
    onOpen: () -> Unit,
    onCart: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onOpen() }
    ) {
        Column(Modifier.padding(12.dp)) {

            /* Fila superior: categoría + menú */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.category,
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
                    lineHeight = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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

                /* Carrito con "badge" de cantidad */
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