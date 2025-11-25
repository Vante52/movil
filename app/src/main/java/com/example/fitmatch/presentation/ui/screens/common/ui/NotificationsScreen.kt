package com.example.fitmatch.presentation.ui.screens.common.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.compose.FitMatchTheme

data class NotificationItem(
    val id: String,
    val userName: String,
    val message: String,
    val timeAgo: String,
    val profileImageUrl: String? = null,
    val type: NotificationType,
    val productImageUrl: String? = null,
    val isUnread: Boolean = true // flag para punto indicador/unread
)

enum class NotificationType { MATCH, ORDER, MESSAGE, CONFIRMATION }

/* ---------------------------------- Screen --------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onMarkAllRead: () -> Unit = {}, // acción para “Marcar todo como leído”
    onNotificationClick: (NotificationItem) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    var selectedTab by rememberSaveable { mutableStateOf(0) } // saveable para rotación
    val tabs = listOf("Todo", "Matches", "Pedidos", "Mensajes")

    // Mock de datos (esto luego viene del repo/paging)
    val all = listOf(
        NotificationItem("1", "fashionista_ana",  "¡Match! Le gustó chaqueta de cuero negra", "1d", type = NotificationType.MATCH),
        NotificationItem("2", "carlos_vintage",   "Tu pedido está en camino - Jeans vintage azul", "1d", type = NotificationType.ORDER),
        NotificationItem("3", "sofia_style",      "Te envió un mensaje sobre vestido floral", "2d", type = NotificationType.MESSAGE, isUnread = false),
        NotificationItem("4", "delivery_fast",    "Pedido entregado - Zapatillas Nike Air Max", "3d", type = NotificationType.ORDER, isUnread = false),
        NotificationItem("5", "shadowlynx",       "Confirmó la compra de tu camisa vintage", "4d", type = NotificationType.CONFIRMATION),
        NotificationItem("6", "minimalist_wardrobe","Te envió un mensaje sobre Blazer negro", "5d", type = NotificationType.MESSAGE),
        NotificationItem("7", "lunavoyager",      "¡Match! Le gustó Zapatillas Nike", "5d", type = NotificationType.MATCH)
    )

    val filtered = when (selectedTab) {
        1 -> all.filter { it.type == NotificationType.MATCH }
        2 -> all.filter { it.type == NotificationType.ORDER }
        3 -> all.filter { it.type == NotificationType.MESSAGE }
        else -> all
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.surface,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp
        ) {
            Column {
                // Título + acción
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Notificaciones",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    // “botoncito” de Marcar todo leído arriba a la derecha
                    TextButton(
                        onClick = onMarkAllRead,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Marcar leído",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary
                        )
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = colors.surface,
                    contentColor = colors.onSurface,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary // usa tu theme
                        )
                    },
                    divider = {
                        HorizontalDivider(
                            color = colors.outlineVariant,
                            thickness = 1.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        val selected = selectedTab == index
                        Tab(
                            selected = selected,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (selected) colors.primary else colors.onSurfaceVariant
                                    )
                                )
                            },
                            selectedContentColor = colors.primary,
                            unselectedContentColor = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Lista
        if (filtered.isEmpty()) {
            EmptyNotificationsState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = { onNotificationClick(notification) }
                    )
                }
                // paginación → usar Paging3 con footer de carga
            }
        }
    }
}

/* --------------------------------- Card ----------------------------------- */

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val dotColor = when (notification.type) {
        NotificationType.MATCH -> colors.primary
        NotificationType.ORDER -> colors.tertiary      // “colorcito” de logística/estado
        NotificationType.MESSAGE -> colors.secondary
        NotificationType.CONFIRMATION -> colors.inversePrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (notification.profileImageUrl != null) {
                    AsyncImage(
                        model = notification.profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenido
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.userName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${notification.timeAgo}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.onSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.onSurfaceVariant
                    ),
                    maxLines = 2
                )

                // “botoncito” CTA contextual (p.ej., Ver pedido / Responder) debajo si aplica el tipo
                // cuando haya deep links a pedido o chat
            }

            // Thumb del producto (si aplica)
            if (notification.productImageUrl != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = notification.productImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Punto indicador (tipo / unread)
            Box(
                modifier = Modifier
                    .size(if (notification.isUnread) 10.dp else 8.dp) // un pelín más grande si es unread
                    .clip(CircleShape)
                    .background(if (notification.isUnread) dotColor else colors.outlineVariant)
            )
        }
    }
    // swipe actions (barrer para archivar) con Modifier.pointerInput + detectDragGestures si quieres
}

/* ----------------------------- Empty State -------------------------------- */

@Composable
private fun EmptyNotificationsState() {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // iconito 👀 o campanita aquí
        Text(
            text = "Sin notificaciones",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Cuando tengas actividad, te avisaremos por aquí.",
            style = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurfaceVariant)
        )
        // “botoncito” para **Configurar notificaciones** (permisos / categorías)
    }
}

/* --------------------------------- Preview -------------------------------- */

@Preview(showBackground = true, showSystemUi = true, name = "Notifications – Light")
@Composable
private fun NotificationsScreenPreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        NotificationsScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Notifications – Dark")
@Composable
private fun NotificationsScreenPreviewDark() {
    FitMatchTheme(darkTheme = true, dynamicColor = false) {
        NotificationsScreen()
    }
}
