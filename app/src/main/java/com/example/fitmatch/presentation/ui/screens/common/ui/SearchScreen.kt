package com.example.fitmatch.presentation.ui.screens.common.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.FitMatchTheme

//  renombré este data class para no chocar con el composable Material3 FilterChip
data class FilterCategory(
    val id: String,
    val name: String,
    val isSelected: Boolean = false
)

data class FilterTag(
    val id: String,
    val name: String,
    //   estos se usan SOLO para la sección “Colores”.
    // Para “Estilos/Prendas” usamos tokens del tema en el UI, no estos campos.
    val colorArgb: Int? = null,           // ej. 0xFF2196F3 (azul)
    val textColorArgb: Int? = null        // ej. 0xFFFFFFFF
)

data class PriceRange(
    val min: String,
    val max: String
)


// Sin @Preview aquí. Los previews van al final envueltos en FitMatchTheme
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    isTemperatureFilterEnabled: Boolean,
    onToggleTemperatureFilter: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    var temperatureFilter by remember { mutableStateOf(isTemperatureFilterEnabled) }

    var searchText by remember { mutableStateOf("") }

    // Categorías principales
    val categories = remember {
        listOf(
            FilterCategory("todos", "Todos", true),
            FilterCategory("tiendas", "Tiendas"),
            FilterCategory("productos", "Productos"),
            FilterCategory("hashtags", "Hashtags")
        )
    }

    // Filtros avanzados - Estilos (usaremos tokens del tema en el chip, no color fijo)
    val styleFilters = remember {
        listOf(
            FilterTag("casual", "Casual"),
            FilterTag("elegante", "Elegante"),
            FilterTag("deportivo", "Deportivo")
        )
    }

    // Filtros - Tipo de prenda (tokens del tema)
    val clothingFilters = remember {
        listOf(
            FilterTag("abrigos", "Abrigos"),
            FilterTag("vestidos", "Vestidos"),
            FilterTag("zapatos", "Zapatos"),
            FilterTag("jeans", "Jeans")
        )
    }

    // Filtros - Colores (estos sí necesitan color explícito porque representan el color en sí)
    val colorFilters = remember {
        listOf(
            FilterTag("azul", "Azul", 0xFF2196F3.toInt()),
            FilterTag("negro", "Negro", 0xFF424242.toInt()),
            FilterTag("rojo", "Rojo", 0xFFF44336.toInt()),
            FilterTag("blanco", "Blanco", 0xFFF5F5F5.toInt(), 0xFF000000.toInt())
        )
    }

    // Rangos de precio
    val priceRanges = remember {
        listOf(
            PriceRange("Hasta $20.000", ""),
            PriceRange("Hasta $40.000", ""),
            PriceRange("Hasta $70.000", ""),
            PriceRange("Hasta $100.000", ""),
            PriceRange("Hasta $150.000", ""),
            PriceRange("Hasta $300.000", "")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        item {
            // Header con búsqueda
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Barra de búsqueda
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.onSurface
                        )
                    }

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Escribe aquí...", color = colors.onSurfaceVariant) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline,
                            cursorColor = colors.primary,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = colors.onSurfaceVariant
                            )
                        },
                        //   “botoncito” para disparar la búsqueda sin teclado
                        trailingIcon = {
                            IconButton(onClick = { onSearchClick() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Buscar",
                                    tint = colors.primary
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    //  este “+” abre modal para filtros avanzados o guardados
                    IconButton(
                        onClick = { /* TODO: abrir modal filtros guardados */ },
                        modifier = Modifier
                            .size(48.dp)
                            .background(colors.surfaceContainerHigh, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir",
                            tint = colors.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Categorías principales
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        FilterCategoryChip(
                            category = category,
                            onClick = { /* TODO: cambiar categoría y refrescar lista */ }
                        )
                    }
                }
            }
        }

        item {
            // Filtros Avanzados
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Filtros Avanzados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Estilos
                Text(
                    text = "Estilos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(styleFilters) { filter ->
                        //   usar primaryContainer/onPrimaryContainer para que no “tape” tanto
                        FilterTagChipThemed(
                            label = filter.name,
                            onClick = { /* TODO: aplicar filtro */ }
                        )
                    }
                }

                // Tipo de prenda
                Text(
                    text = "Tipo de prenda",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(clothingFilters) { filter ->
                        FilterTagChipThemed(
                            label = filter.name,
                            onClick = { /* TODO: aplicar filtro */ }
                        )
                    }
                }

                // Colores
                Text(
                    text = "Colores",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(colorFilters) { filter ->
                        FilterColorChip(
                            filter = filter,
                            onClick = { /* TODO: aplicar filtro color */ }
                        )
                    }
                }
            }
        }

        item {
            // Rangos de precio
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Rangos de precio",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Grid de precios (2 columnas)
                for (i in priceRanges.indices step 2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PriceRangeChip(
                            priceRange = priceRanges[i],
                            modifier = Modifier.weight(1f),
                            onClick = { /* TODO: set rango */ }
                        )

                        if (i + 1 < priceRanges.size) {
                            PriceRangeChip(
                                priceRange = priceRanges[i + 1],
                                modifier = Modifier.weight(1f),
                                onClick = { /* TODO: set rango */ }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Filtro de temperatura
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Filtrar por temperatura",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (temperatureFilter) "Activado" else "Desactivado",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (temperatureFilter) colors.primary else colors.onSurfaceVariant
                        )
                    )

                    Switch(
                        checked = temperatureFilter,
                        onCheckedChange = {
                            temperatureFilter = it
                            onToggleTemperatureFilter(it)
                        }
                    )
                }
            }
        }


        item {
            // Ubicación
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Ubicación",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Placeholder para mapa
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.secondaryContainer // verdosito/beige del tema
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Ubicación",
                                tint = colors.secondary, //   color no tan fuerte
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Mapa de ubicación",
                                fontSize = 12.sp,
                                color = colors.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Bogotá DC, Colombia",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(100.dp))
                // “botoncito” aplicar filtros podría ir sticky en la parte baja
            }
        }
    }
}

// ===============
// Chips / items
// ===============

@Composable
private fun FilterCategoryChip(
    category: FilterCategory,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    //   seleccionado -> primary/onPrimary; no seleccionado -> surface + borde outline
    Surface(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        color = if (category.isSelected) colors.primary else colors.surface,
        shape = RoundedCornerShape(20.dp),
        border = if (!category.isSelected)
            BorderStroke(1.dp, colors.outline)
        else null
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = category.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (category.isSelected) colors.onPrimary else colors.onSurface
            )
        }
    }
}

@Composable
private fun FilterTagChipThemed(
    label: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    //   usamos primaryContainer para que no “grite”; texto con onPrimaryContainer
    Surface(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        color = colors.primaryContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun FilterColorChip(
    filter: FilterTag,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    //   estos sí llevan color “fuerte” para representar el color del producto
    val bg = filter.colorArgb?.let { Color(it) } ?: colors.surfaceVariant
    val fg = filter.textColorArgb?.let { Color(it) }
        ?: (if (filter.colorArgb == null) colors.onSurfaceVariant else colors.onPrimary)

    Surface(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        color = bg,
        shape = RoundedCornerShape(16.dp),
        border = if (filter.colorArgb == null)
            BorderStroke(1.dp, colors.outline)
        else null
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = filter.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = fg
            )
        }
    }
}

@Composable
private fun PriceRangeChip(
    priceRange: PriceRange,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    // surfaceVariant como fondo, outline como borde
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        color = colors.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = priceRange.min,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
        }
    }
}

@Preview(showBackground = true, name = "Search – Light (Brand)")
@Composable
private fun SearchPreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        SearchScreen(
            onBackClick = {},
            onSearchClick = {},
            isTemperatureFilterEnabled = false,
            onToggleTemperatureFilter = {}
        )
    }
}

@Preview(showBackground = true, name = "Search – Dark (Brand)")
@Composable
private fun SearchPreviewDark() {
    FitMatchTheme(darkTheme = true, dynamicColor = false) {
        SearchScreen(
            onBackClick = {},
            onSearchClick = {},
            isTemperatureFilterEnabled = true,
            onToggleTemperatureFilter = {}
        )
    }
}
