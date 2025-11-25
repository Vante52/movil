package com.example.fitmatch.presentation.ui.screens.vendedor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.FitMatchTheme

// Modelos (sin colores hardcodeados)
data class DashboardMetric(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val changeIndicator: String = "",
    val isPositive: Boolean = true
)

data class QuickAction(
    val title: String,
    val icon: ImageVector
)

@Composable
fun VendedorDashboardScreen(
    onAgregarProductoClick: () -> Unit = {},
    onMostrarProductosClick: () -> Unit = {},
    onMisPedidosClick: () -> Unit = {},
    onEstadisticasClick: () -> Unit = {},
    onEnviosClick: () -> Unit = {},
    onComentariosClick: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    val greeting = "¡Hola, María!"
    val date = "Lunes, 1 Septiembre 2025"

    // datos mock — conectar a repo/VM cuando esté el backend listo
    val mainMetrics = listOf(
        DashboardMetric(
            title = "Ganancias esta semana",
            value = "$200.000 COP",
            subtitle = "",
            icon = Icons.Default.AttachMoney,
        ),
        DashboardMetric(
            title = "Calificación",
            value = "4.8★",
            subtitle = "",
            icon = Icons.Default.Star,
        )
    )

    val keyIndicators = listOf(
        DashboardMetric(
            title = "Ganancias del mes",
            value = "$500.000 COP",
            subtitle = "+18% vs mes anterior",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            changeIndicator = "+18%",
            isPositive = true
        ),
        DashboardMetric(
            title = "Tasa de conversión",
            value = "12.5%",
            subtitle = "+2.3% esta semana",
            icon = Icons.AutoMirrored.Filled.ShowChart,
            changeIndicator = "+2.3%",
            isPositive = true
        ),
        DashboardMetric(
            title = "Productos activos",
            value = "15",
            subtitle = "+8 nuevos esta semana",
            icon = Icons.Default.Inventory,
            changeIndicator = "+8",
            isPositive = true
        ),
        DashboardMetric(
            title = "Stock total bajo",
            value = "20",
            subtitle = "Requiere atención",
            icon = Icons.Default.Warning,
            changeIndicator = "⚠️",
            isPositive = false
        )
    )

    val quickActions = listOf(
        QuickAction("Agregar\nProducto", Icons.Default.Add),
        QuickAction("Mostrar\nProductos", Icons.AutoMirrored.Filled.ViewList),
        QuickAction("Mis\nPedidos", Icons.Default.LocalShipping),
        QuickAction("Estadisticas", Icons.Default.Star),
        QuickAction("Envíos", Icons.Default.LocalShipping),
        QuickAction("Comentarios", Icons.Default.RateReview)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background) // antes: Color(0xFFF5F5DC)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Saludo y fecha
            Column {
                Text(
                    text = greeting,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground
                )
                Text(
                    text = date,
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariant
                )
            }
            // Nota estudiante: botoncito de “cambiar rango de fecha” podría ir aquí a la derecha
        }

        item {
            // Métricas principales en fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                mainMetrics.forEach { metric ->
                    MainMetricCard(
                        metric = metric,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            // Alerta de stock
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colors.tertiaryContainer // Nota estudiante: amarillito del tema
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alerta",
                        tint = colors.tertiary, // Nota estudiante: colorcito de warning, no tan agresivo como error
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Stock Bajo detectado",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onTertiaryContainer
                        )
                        Text(
                            text = "5 productos están por debajo de 5 unidades",
                            fontSize = 12.sp,
                            color = colors.onTertiaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }
                // Nota estudiante: si queremos alerta “fuerte”, cambiar a errorContainer/onErrorContainer
            }
        }

        item {
            // Título indicadores principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = "Indicadores",
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Indicadores Principales",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }
        }

        item {
            // Grid de indicadores 2x2
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IndicatorCard(metric = keyIndicators[0], modifier = Modifier.weight(1f))
                    IndicatorCard(metric = keyIndicators[1], modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IndicatorCard(metric = keyIndicators[2], modifier = Modifier.weight(1f))
                    IndicatorCard(metric = keyIndicators[3], modifier = Modifier.weight(1f))
                }
            }
            // Nota estudiante: poner switch “ver vs semana pasada / vs mes pasado”
        }

        item {
            // Título acciones rápidas
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Acciones",
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Acciones Rápidas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }
        }

        item {
            // Grid de acciones rápidas 2x3
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        action = quickActions[0],
                        modifier = Modifier.weight(1f),
                        onClick = onAgregarProductoClick
                    )
                    QuickActionCard(
                        action = quickActions[1],
                        modifier = Modifier.weight(1f),
                        onClick = onMostrarProductosClick
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        action = quickActions[2],
                        modifier = Modifier.weight(1f),
                        onClick = onMisPedidosClick
                    )
                    QuickActionCard(
                        action = quickActions[3],
                        modifier = Modifier.weight(1f),
                        onClick = onEstadisticasClick
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        action = quickActions[4],
                        modifier = Modifier.weight(1f),
                        onClick = onEnviosClick
                    )
                    QuickActionCard(
                        action = quickActions[5],
                        modifier = Modifier.weight(1f),
                        onClick = onComentariosClick
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun MainMetricCard(
    metric: DashboardMetric,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nota estudiante: icono con color primario para mantener marca
            Icon(
                imageVector = metric.icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = metric.value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                text = metric.title,
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun IndicatorCard(
    metric: DashboardMetric,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Nota estudiante: aquí el iconito con primary para consistencia
                Icon(
                    imageVector = metric.icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = metric.value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = metric.title,
                fontSize = 12.sp,
                color = colors.onSurface,
                fontWeight = FontWeight.Medium
            )

            if (metric.subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = metric.subtitle,
                    fontSize = 10.sp,
                    color = if (metric.isPositive) colors.tertiary else colors.error // Nota estudiante: verdecito vs rojito del tema
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    action: QuickAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface), // antes: Color.White o custom
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Nota estudiante: usar primary para que el iconito resalte pero sin “gritar”
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                tint = colors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = action.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}


// Previews con el FitMatchTheme

@Preview(showBackground = true, name = "Seller Dashboard – Light (Brand)")
@Composable
private fun SellerDashboardPreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        VendedorDashboardScreen()
    }
}

@Preview(showBackground = true, name = "Seller Dashboard – Dark (Brand)")
@Composable
private fun SellerDashboardPreviewDark() {
    FitMatchTheme(darkTheme = true, dynamicColor = false) {
        VendedorDashboardScreen()
    }
}
