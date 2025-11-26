package com.example.fitmatch.presentation.ui.screens.wear


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.items
import com.example.fitmatch.presentation.ui.screens.cliente.state.ProductCardState
import com.example.fitmatch.presentation.viewmodel.user.ClienteDashboardViewModel
import com.example.fitmatch.presentation.viewmodel.user.DashboardEvent

@Composable
fun WearClienteDashboardScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ClienteDashboardViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ClienteDashboardViewModel(context) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsState(initial = 15f)

    val scalingListState = rememberScalingLazyListState()
    val connectionStatus = rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is DashboardEvent.NavigateToStore) {
                // En un reloj solo mostramos feedback visual, la navegación se delega al teléfono
                connectionStatus.value = true
            }
        }
    }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(
            vignettePosition = TODO(),
            modifier = TODO()
        ) },
        positionIndicator = { PositionIndicator(scalingListState) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = scalingListState
        ) {
            item {
                WearHeader(
                    connected = connectionStatus.value,
                    temperature = temperature,
                    onBack = onBack,
                    onReload = { viewModel.onReloadProducts() }
                )
            }

            uiState.currentProduct?.let { product ->
                item {
                    WearProductCard(
                        product = product,
                        onLike = { viewModel.onSwipeRight() },
                        onPass = { viewModel.onSwipeLeft() },
                        onOpenStore = { viewModel.onOpenStore(product) }
                    )
                }
            }

            if (uiState.productDeck.isNotEmpty()) {
                items(uiState.productDeck.take(5)) { product ->
                    SmallProductRow(
                        product = product,
                        onSelect = { viewModel.onViewDetails(product) }
                    )
                }
            } else {
                item {
                    EmptyStateCard(onReload = { viewModel.onReloadProducts() })
                }
            }

            item {
                CompactChip(
                    onClick = { viewModel.onToggleTiltSensor(!uiState.isTiltEnabled) },
                    label = { Text(if (uiState.isTiltEnabled) "Giroscopio activo" else "Activar giroscopio") },
                    icon = {
                        Icon(
                            imageVector = if (uiState.isTiltEnabled) Icons.Default.CheckCircle else Icons.Default.Refresh,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                CompactChip(
                    onClick = { connectionStatus.value = !connectionStatus.value },
                    label = { Text(if (connectionStatus.value) "Watch sincronizado" else "Reconectar watch") },
                    icon = {
                        Icon(
                            imageVector = if (connectionStatus.value) Icons.Default.CheckCircle else Icons.Default.CloudOff,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun WearHeader(
    connected: Boolean,
    temperature: Float?,
    onBack: () -> Unit,
    onReload: () -> Unit
) {
    Card(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusRow(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    label = "Volver",
                    emphasized = true
                )
                Chip(
                    onClick = onReload,
                    label = { Text("Recargar") },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                )
            }
            StatusRow(
                icon = if (connected) Icons.Default.CheckCircle else Icons.Default.CloudOff,
                label = if (connected) "Wear conectado" else "Wear sin conexión",
                emphasized = true
            )
            temperature?.let {
                StatusRow(
                    icon = Icons.Default.ShoppingCart,
                    label = "Temp: ${it.toInt()}°C",
                    emphasized = false
                )
            }
        }
    }
}

@Composable
private fun WearProductCard(
    product: ProductCardState = ProductCardState("0", "", "", 0),
    onLike: () -> Unit,
    onPass: () -> Unit,
    onOpenStore: () -> Unit
) {
    Card(
        onClick = onOpenStore,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = product.title.ifEmpty { "Producto destacado" },
                style = MaterialTheme.typography.title3,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = product.brand.ifEmpty { "Marca" },
                style = MaterialTheme.typography.caption1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(onClick = onPass, modifier = Modifier.weight(1f)) {
                    Text("Pass")
                }
                Button(onClick = onLike, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Like")
                }
            }
        }
    }
}

@Composable
private fun SmallProductRow(
    product: ProductCardState,
    onSelect: () -> Unit
) {
    Card(onClick = onSelect, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(product.title, style = MaterialTheme.typography.body2, fontWeight = FontWeight.SemiBold)
                Text(product.brand, style = MaterialTheme.typography.caption2)
            }
            Text("$${product.price}", style = MaterialTheme.typography.caption1)
        }
    }
}

@Composable
private fun EmptyStateCard(onReload: () -> Unit) {
    Card(onClick = onReload, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null
            )
            Text("Sin productos disponibles", style = MaterialTheme.typography.caption1)
            Button(onClick = onReload) { Text("Actualizar") }
        }
    }
}

@Composable
private fun StatusRow(
    icon: ImageVector,
    label: String,
    emphasized: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(
            text = label,
            style = if (emphasized) MaterialTheme.typography.body1 else MaterialTheme.typography.caption1,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
