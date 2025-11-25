package com.example.fitmatch.presentation.ui.screens.common.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.FitMatchTheme

enum class OrderStatus { TODOS, PENDIENTE, PREPARANDO, ENVIADO }

data class OrderUi(
    val id: String,           // #MNX-24819
    val dateTime: String,     // "24 Ago, 09:15"
    val buyerName: String,    // "Laura P."
    val buyerHandle: String,  // "@laura_p"
    val itemsText: String,    // "2 artículos"
    val price: String,        // "$439.600"
    val status: OrderStatus
)

private fun sampleOrders() = listOf(
    OrderUi("#MNX-24819", "24 Ago, 09:15", "Laura P.",  "@laura_p",   "2 artículos", "$439.600", OrderStatus.PREPARANDO),
    OrderUi("#MNX-24818", "24 Ago, 08:27", "Carlos D.", "@carlos_d",  "1 artículo",  "$189.900", OrderStatus.PENDIENTE),
    OrderUi("#MNX-24810", "23 Ago, 17:02", "María G.",  "@maria_g",   "1 artículo",  "$129.900", OrderStatus.ENVIADO),
)

/* --------------------------------- Screen --------------------------------- */

@Composable
fun OrdersScreen(
    onMenuClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onOrderClick: (OrderUi) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    var query by rememberSaveable { mutableStateOf("") } // estudiante: usar saveable para rotación
    var selected by rememberSaveable { mutableStateOf(OrderStatus.TODOS) }
    val orders = remember { sampleOrders() }

    Scaffold(
        topBar = {
            // estudiante: si quieres elevación y scroll-behavior, migrar a CenterAlignedTopAppBar
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface) // antes background → mejor surface para top bars
                    .padding(top = 12.dp, bottom = 8.dp)
            ) {
                //  Back a la izquierda:
                IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                     Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                 }
                Text(
                    "Mis Pedidos",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = colors.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onMenuClick, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Más", tint = colors.onSurface)
                }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            // ───── Buscador
            SearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Buscar pedido, comprador..."
            )

            Spacer(Modifier.height(12.dp))

            // ───── Filtros
            FilterRow(
                selected = selected,
                onSelected = { selected = it }
            )

            Spacer(Modifier.height(12.dp))

            // ───── Lista scrolleable
            val filtered = orders.filter { o ->
                (selected == OrderStatus.TODOS || o.status == selected) &&
                        (query.isBlank() || o.id.contains(query, true) ||
                                o.buyerName.contains(query, true) || o.buyerHandle.contains(query, true))
            }

            if (filtered.isEmpty()) {
                // estudiante: estado vacío bonito para UX, guiamos a limpiar filtros
                EmptyState(
                    title = "Sin resultados",
                    subtitle = "Prueba ajustando los filtros o busca por ID, nombre o usuario."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { order ->
                        OrderCard(order = order, onClick = { onOrderClick(order) })
                    }
                }
            }
        }
    }
}

/* --------------------------------- Partials -------------------------------- */

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        placeholder = { Text(placeholder, color = colors.onSurfaceVariant) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = colors.onSurfaceVariant) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = colors.surfaceVariant.copy(alpha = 0.35f),
            focusedContainerColor = colors.surfaceVariant.copy(alpha = 0.35f),
            unfocusedBorderColor = colors.outlineVariant,
            focusedBorderColor = colors.primary
        )
    )
    // estudiante: meter debounce si la búsqueda va a red/hardwork
}

@Composable
private fun FilterRow(
    selected: OrderStatus,
    onSelected: (OrderStatus) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterPill("Todos", selected == OrderStatus.TODOS, { onSelected(OrderStatus.TODOS) }, colors)
        FilterPill("Pendientes", selected == OrderStatus.PENDIENTE, { onSelected(OrderStatus.PENDIENTE) }, colors)
        FilterPill("Preparando", selected == OrderStatus.PREPARANDO, { onSelected(OrderStatus.PREPARANDO) }, colors)
        FilterPill("Enviado", selected == OrderStatus.ENVIADO, { onSelected(OrderStatus.ENVIADO) }, colors)
    }
    // estudiante: estos chips podrían ser Sticky + scroll horizontal cuando haya más estados
}

@Composable
private fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: ColorScheme
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (selected) colors.primary.copy(alpha = 0.12f) else colors.surface,
        border = BorderStroke(
            1.dp,
            if (selected) colors.primary else colors.outlineVariant
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = colors.onSurface
            )
        )
    }
    // estudiante: “botoncito” de limpiar filtros a la derecha cuando haya algo seleccionado
}

@Composable
private fun OrderCard(
    order: OrderUi,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val (statusText, statusColor) = when (order.status) {
        OrderStatus.PENDIENTE   -> "Pendiente"  to colors.secondary
        OrderStatus.PREPARANDO  -> "Preparando" to colors.primary
        OrderStatus.ENVIADO     -> "Enviado"    to colors.tertiary
        OrderStatus.TODOS       -> ""           to colors.outline
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {

            // Primera línea: ID + fecha/hora + precio a la derecha
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${order.id} · ${order.dateTime}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = colors.onSurfaceVariant
                    ),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = order.price,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                )
            }

            Spacer(Modifier.height(6.dp))

            // Nombre comprador
            Text(
                text = order.buyerName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
            )
            Text(
                text = "${order.buyerHandle} — ${order.itemsText}",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurfaceVariant),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            // Estado + "Ver detalle"
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (statusText.isNotEmpty()) {
                    StatusPill(text = statusText, color = statusColor)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Ver detalle →",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                )
            }
        }
    }
    // estudiante: meter “botoncito” contextual (3 puntitos) dentro de la card para acciones rápidas
}

@Composable
private fun StatusPill(text: String, color: Color) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
        )
    }
}

/* ------------------------------ Empty State ------------------------------- */

@Composable
private fun EmptyState(
    title: String,
    subtitle: String
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        //aquí pondría un iconito ilustrativo y un CTA para limpiar filtros
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurfaceVariant),
            textAlign = TextAlign.Center
        )
    }
}

/* --------------------------------- Preview -------------------------------- */

@Preview(showBackground = true, showSystemUi = true, name = "Orders – Light")
@Composable
private fun OrdersScreenPreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        OrdersScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Orders – Dark")
@Composable
private fun OrdersScreenPreviewDark() {
    FitMatchTheme(darkTheme = true, dynamicColor = false) {
        OrdersScreen()
    }
}
