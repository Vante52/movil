package com.example.fitmatch.presentation.ui.screens.cliente

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.FitMatchTheme
import com.example.fitmatch.presentation.ui.screens.cliente.state.CartItemState
import com.example.fitmatch.presentation.viewmodel.user.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBackClick: () -> Unit = {},
    onCheckoutClick: () -> Unit = {},
    viewModel: CartViewModel = viewModel()
) {
    val colors = MaterialTheme.colorScheme

    // observer
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Mostrar Snackbar si hay error
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onDismissError()
        }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = colors.onSurface
                        )
                    }

                    Text(
                        text = "Cesta (${uiState.itemCount})",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                        IconButton(onClick = { /* Menu */ }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Menú",
                                tint = colors.onSurface
                            )
                        }
                    }
                }
            }
        },
    ) { inner ->
        if (uiState.isEmpty) {
            // Estado vacío
            EmptyCartState(
                onBackClick = onBackClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Items del carrito
                items(uiState.items, key = { it.id }) { item ->
                    CartItemCard(
                        item = item,
                        onMinus = { viewModel.onDecreaseQuantity(item.id) },
                        onPlus = { viewModel.onIncreaseQuantity(item.id) },
                        onRemove = { viewModel.onRemoveItem(item.id) }
                    )
                }

                item { Spacer(Modifier.height(4.dp)) }

                // Cupón
                item {
                    CouponCard(
                        couponCode = uiState.couponCode,
                        onCouponCodeChanged = { viewModel.onCouponCodeChanged(it) },
                        onApply = { viewModel.onApplyCoupon() },
                        isLoading = uiState.isLoading
                    )
                }

                // Resumen
                item {
                    SummaryCard(
                        subtotal = uiState.subtotal,
                        envio = uiState.shippingCost,
                        descuento = uiState.appliedDiscount,
                        total = uiState.total
                    )
                }

                // Botón Checkout
                item {
                    Button(
                        onClick = {
                            viewModel.onCheckout(onSuccess = onCheckoutClick)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        enabled = !uiState.isProcessingCheckout
                    ) {
                        if (uiState.isProcessingCheckout) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = colors.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "TRAMITAR PEDIDO",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}
//composables
@Composable
private fun EmptyCartState(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🛒",
            fontSize = 64.sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Tu carrito está vacío",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Agrega productos para comenzar tu compra",
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBackClick,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Explorar productos")
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItemState,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onRemove: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.35f))
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$${formatPrice(item.price)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.End
                )
            }

            Text(
                text = item.shop,
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurfaceVariant)
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(text = item.size)
                Spacer(Modifier.width(8.dp))
                Pill(text = item.color)

                Spacer(Modifier.weight(1f))

                QuantityControl(qty = item.quantity, onMinus = onMinus, onPlus = onPlus)

                Spacer(Modifier.width(8.dp))

                Surface(
                    onClick = onRemove,
                    shape = CircleShape,
                    color = colors.surface,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.35f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar del carrito",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Pill(text: String) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.35f))
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurface
        )
    }
}

@Composable
private fun QuantityControl(qty: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SmallSquareButton(text = "−", onClick = onMinus, enabled = qty > 1)
        Surface(
            modifier = Modifier.widthIn(min = 40.dp),
            shape = RoundedCornerShape(8.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.35f))
        ) {
            Text(
                "$qty",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurface
            )
        }
        SmallSquareButton(text = "+", onClick = onPlus, enabled = true)
    }
}

@Composable
private fun SmallSquareButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.35f))
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) colors.onSurface else colors.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun CouponCard(
    couponCode: String,
    onCouponCodeChanged: (String) -> Unit,
    onApply: () -> Unit,
    isLoading: Boolean
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.25f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "Cupón",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onSurface
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = couponCode,
                    onValueChange = onCouponCodeChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Código", color = colors.onSurfaceVariant) },
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline,
                        cursorColor = colors.primary,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onApply,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && couponCode.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = colors.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Aplicar", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    subtotal: Int,
    envio: Int,
    descuento: Int,
    total: Int
) {
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.25f))
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Resumen", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Spacer(Modifier.height(12.dp))
            RowLine("Subtotal", "$${formatPrice(subtotal)}")
            RowLine("Envío (estimado)", "$${formatPrice(envio)}")
            if (descuento > 0) {
                RowLine("Descuento", "-$${formatPrice(descuento)}", isDiscount = true)
            }
            HorizontalDivider(
                Modifier.padding(vertical = 8.dp),
                DividerDefaults.Thickness,
                color = colors.outline.copy(alpha = 0.3f)
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface
                )
                Text(
                    "$${formatPrice(total)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun RowLine(label: String, value: String, isDiscount: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDiscount) colors.primary else colors.onSurface
        )
    }
}

private fun formatPrice(value: Int): String =
    "%,d".format(value).replace(',', '.')

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CartScreenPreview() {
    FitMatchTheme {
        CartScreen()
    }
}