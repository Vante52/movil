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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.compose.FitMatchTheme

/* ---------------------------- Model / Mock data ---------------------------- */

data class CartItem(
    val id: String,
    val title: String,
    val shop: String,
    val price: Int,         // en pesos
    val size: String,
    val color: String,
)

private fun mockCart(): List<CartItem> = listOf(
    CartItem("1", "Blazer Premium en Lino", "@ateliernova", 189_900, "M", "Negro"),
    CartItem("2", "Pantalón Wide Leg", "@lunaurban", 129_900, "S", "Beige"),
)

/* --------------------------------- Screen --------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBackClick: () -> Unit = {},
    onMenu: () -> Unit = {},
    onCheckoutClick: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme

    // usar saveable para no perder cantidades si rota (guardamos como lista simple)
    var items by rememberSaveable(stateSaver = listSaver(
        save = { list -> list.flatMap { li -> listOf(li.id, li.title, li.shop, li.price.toString(), li.size, li.color) } },
        restore = { flat ->
            flat.chunked(6).map { fields ->
                val id = fields[0]
                val title = fields[1]
                val shop = fields[2]
                val price = fields[3].toInt()
                val size = fields[4]
                val color = fields[5]
                CartItem(id, title, shop, price, size, color)
            }
        }
    )) { mutableStateOf(mockCart()) }

    // cantidades por id (state map simple; si quieres también saveable, crea saver propio)
    val qty = remember { mutableStateMapOf("1" to 1, "2" to 2) }

    // Cálculos
    val subtotal = items.sumOf { it.price * (qty[it.id] ?: 1) }
    val envio = 9_900
    val descuento = 20_000
    val total = subtotal + envio - descuento

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colors.background,
        topBar = {
            // ===== Header unificado: título centrado + back + acciones (color/elevación como Notificaciones) =====
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
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = colors.onSurface
                        )
                    }

                    // Título centrado (tipografía/tamaño como pediste)
                    Text(
                        text = "Cesta",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = colors.onSurface
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Acciones a la derecha (se mantienen)
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onMenu) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // 1) Items del carrito
            items(items, key = { it.id }) { item ->
                CartItemCard(
                    item = item,
                    quantity = qty[item.id] ?: 1,
                    onMinus = { val c = qty[item.id] ?: 1; if (c > 1) qty[item.id] = c - 1 },
                    onPlus  = { val c = qty[item.id] ?: 1; qty[item.id] = c + 1 },
                    onRemove = {
                        items = items.filterNot { it.id == item.id }
                        qty.remove(item.id)
                    }
                )
            }

            // 2) Espacio
            item { Spacer(Modifier.height(4.dp)) }

            // 3) Cupón
            item {
                CouponCard(onApply = { /* validar cupón */ })
            }

            // 4) Resumen
            item {
                SummaryCard(
                    subtotal = subtotal,
                    envio = envio,
                    descuento = descuento,
                    total = total
                )
            }

            // 5) Botón Checkout
            item {
                Button(
                    onClick = onCheckoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    enabled = items.isNotEmpty()
                ) {
                    Text("TRAMITAR PEDIDO", fontWeight = FontWeight.SemiBold)
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

/* --------------------------------- Items --------------------------------- */

@Composable
private fun CartItemCard(
    item: CartItem,
    quantity: Int,
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

                QuantityControl(qty = quantity, onMinus = onMinus, onPlus = onPlus)

                Spacer(Modifier.width(8.dp))

                // botoncito “eliminar”
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
        SmallSquareButton(text = "−", onClick = onMinus)
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
        SmallSquareButton(text = "+", onClick = onPlus)
    }
}

@Composable
private fun SmallSquareButton(text: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.35f))
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurface
        )
    }
}

/* --------------------------------- Cupón --------------------------------- */

@Composable
private fun CouponCard(
    onApply: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var code by remember { mutableStateOf("") }

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
                    value = code,
                    onValueChange = { code = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Código", color = colors.onSurfaceVariant) },
                    singleLine = true,
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
                    onClick = { onApply(code) },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Aplicar", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/* -------------------------------- Resumen -------------------------------- */

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
            RowLine("Descuento", "-$${formatPrice(descuento)}")
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
private fun RowLine(label: String, value: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
    }
}

private fun formatPrice(value: Int): String =
    "%,d".format(value).replace(',', '.')

/* --------------------------------- Preview -------------------------------- */

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_6")
@Composable
private fun CartScreenPreview() {
    FitMatchTheme {
        CartScreen()
    }
}
