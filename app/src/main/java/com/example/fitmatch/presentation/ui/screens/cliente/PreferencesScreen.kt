package com.example.fitmatch.presentation.ui.screens.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.FitMatchTheme
import kotlinx.coroutines.launch

// Enum para los diferentes tipos de pantalla
enum class PreferenceType {
    STYLES, COLORS, SIZES, CATEGORIES
}

// Data class para las opciones
data class PreferenceOption(
    val id: String,
    val title: String,
    val icon: ImageVector, //   en real, vendría un drawable/url
    val backgroundColor: Color = Color.White // lo usamos como "swatch" de color, no como fondo del card
)

@Composable
fun PreferencesScreen(
    preferenceType: PreferenceType = PreferenceType.STYLES,
    onBackClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onContinueClick: (Set<String>) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    val (title, subtitle, options) = when (preferenceType) {
        PreferenceType.STYLES -> Triple(
            "Cuéntanos qué te gusta",
            "Esto nos ayudará a traerte más ropa de tu estilo",
            getStyleOptions()
        )
        PreferenceType.COLORS -> Triple(
            "Cuéntanos qué te gusta",
            "¿Cuál color te gustaría ver más en tus prendas?",
            getColorOptions()
        )
        PreferenceType.SIZES -> Triple(
            "Cuéntanos qué te gusta",
            "¡Es importante tener tu talla en nuestro stock!",
            getSizeOptions()
        )
        PreferenceType.CATEGORIES -> Triple(
            "Cuéntanos qué te gusta",
            "¿Qué prendas sueles comprar?",
            getCategoryOptions()
        )
    }

    var selectedOptions by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background) // antes: Color(0xFFF5F5DC)
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = colors.onSurface // usar onSurface para que se vea en claro/oscuro
                )
            }

            TextButton(onClick = onSkipClick) {
                Text(
                    text = "Saltar",
                    color = colors.primary, // botoncito con color de marca
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Título y subtítulo
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = subtitle,
            fontSize = 16.sp,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Grid de opciones
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(options) { option ->
                PreferenceOptionCard(
                    option = option,
                    isSelected = selectedOptions.contains(option.id),
                    onClick = {
                        selectedOptions = if (selectedOptions.contains(option.id)) {
                            selectedOptions - option.id
                        } else {
                            selectedOptions + option.id
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Continuar
        Button(
            onClick = { onContinueClick(selectedOptions) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary
            ),
            shape = RoundedCornerShape(25.dp),
            enabled = selectedOptions.isNotEmpty()
        ) {
            Text(
                text = "Continuar",
                color = colors.onPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PreferenceOptionCard(
    option: PreferenceOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    // borde de selección “marca” pero finito para no gritar
                    Modifier.border(
                        width = 2.dp,
                        brush = SolidColor(colors.primary),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.primary else colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Si la opción trae un backgroundColor distinto a blanco (caso "COLORS"),
            // mostramos un “swatch” circular en vez del ícono tintado.
            val hasColorSwatch = option.backgroundColor != Color.White

            if (hasColorSwatch) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = option.backgroundColor,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) colors.onPrimary.copy(alpha = 0.4f) else colors.outlineVariant,
                            shape = CircleShape
                        )
                )
            } else {
                Icon(
                    imageVector = option.icon,
                    contentDescription = option.title,
                    tint = if (isSelected) colors.onPrimary else colors.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = option.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) colors.onPrimary else colors.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

// Opciones para estilos
private fun getStyleOptions(): List<PreferenceOption> {
    return listOf(
        PreferenceOption("streetwear", "Streetwear", Icons.Default.Style),
        PreferenceOption("vintage", "Vintage", Icons.Default.History),
        PreferenceOption("deportivo", "Deportivo", Icons.Default.SportsBaseball),
        PreferenceOption("casual", "Casual", Icons.Default.Person),
        PreferenceOption("elegante", "Elegante", Icons.Default.Star),
        PreferenceOption("minimalista", "Minimalista", Icons.Default.Remove)
    )
}

// Opciones para colores (usamos backgroundColor como swatch, el card mantiene surface/primary)
private fun getColorOptions(): List<PreferenceOption> {
    return listOf(
        PreferenceOption("negro", "Negro", Icons.Default.Circle, Color(0xFF2E2E2E)),
        PreferenceOption("blanco", "Blanco", Icons.Default.Circle, Color(0xFFF5F5F5)),
        PreferenceOption("azul", "Azul", Icons.Default.Circle, Color(0xFF4A90E2)),
        PreferenceOption("rojo", "Rojo", Icons.Default.Circle, Color(0xFFE74C3C)),
        PreferenceOption("verde", "Verde", Icons.Default.Circle, Color(0xFF27AE60)),
        PreferenceOption("amarillo", "Amarillo", Icons.Default.Circle, Color(0xFFF39C12)),
        PreferenceOption("marron", "Marrón", Icons.Default.Circle, Color(0xFF8B4513)),
        PreferenceOption("cualquiera", "Cualquiera", Icons.Default.Palette) // sin swatch → iconito
    )
}

// Opciones para tallas
private fun getSizeOptions(): List<PreferenceOption> {
    return listOf(
        PreferenceOption("xxs", "XXS", Icons.Default.Straighten),
        PreferenceOption("xs", "XS", Icons.Default.Straighten),
        PreferenceOption("s", "S", Icons.Default.Straighten),
        PreferenceOption("m", "M", Icons.Default.Straighten),
        PreferenceOption("l", "L", Icons.Default.Straighten),
        PreferenceOption("xl", "XL", Icons.Default.Straighten),
        PreferenceOption("xxl", "XXL", Icons.Default.Straighten),
        PreferenceOption("xxxl", "XXXL", Icons.Default.Straighten)
    )
}

// Opciones para categorías
private fun getCategoryOptions(): List<PreferenceOption> {
    return listOf(
        PreferenceOption("cazadoras_abrigos", "Cazadoras y \nabrigos", Icons.Default.DryCleaning),
        PreferenceOption("sudaderas", "Sudaderas", Icons.Default.Checkroom),
        PreferenceOption("jerseis_chaquetas", "Jerseis y\nChaquetas", Icons.Default.Dry),
        PreferenceOption("camisas_camisetas", "Camisas y\ncamisetas", Icons.Default.ShoppingBag),
        PreferenceOption("vestidos", "Vestidos", Icons.Default.Woman),
        PreferenceOption("jeans", "Jeans", Icons.Default.HeartBroken),
        PreferenceOption("pantalones", "Pantalones", Icons.Default.InsertEmoticon),
        PreferenceOption("ropa_deportiva", "Ropa\ndeportiva", Icons.Default.FitnessCenter)
    )
}

// ==========================
// Previews con tu tema (dynamicColor = false)
// ==========================
@Preview(showBackground = true, name = "Preferences – Styles – Light")
@Composable
private fun PreferencesPreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        PreferencesScreen(preferenceType = PreferenceType.STYLES)
    }
}
@Composable
fun PreferencesFlowScreen(
    onBackToRegister: () -> Unit = {},
    onFinishClick: (Map<PreferenceType, Set<String>>) -> Unit = {}
) {
    // Orden de categorías
    val steps = listOf(
        PreferenceType.STYLES,
        PreferenceType.COLORS,
        PreferenceType.SIZES,
        PreferenceType.CATEGORIES
    )

    var stepIndex by rememberSaveable { mutableStateOf(0) }
    val total = steps.size
    val currentType = steps[stepIndex]

    // Selecciones por categoría (persistentes entre recomposiciones)
    var stylesSel by rememberSaveable { mutableStateOf(setOf<String>()) }
    var colorsSel by rememberSaveable { mutableStateOf(setOf<String>()) }
    var sizesSel by rememberSaveable { mutableStateOf(setOf<String>()) }
    var categoriesSel by rememberSaveable { mutableStateOf(setOf<String>()) }

    fun getSelection(type: PreferenceType): Set<String> = when (type) {
        PreferenceType.STYLES -> stylesSel
        PreferenceType.COLORS -> colorsSel
        PreferenceType.SIZES -> sizesSel
        PreferenceType.CATEGORIES -> categoriesSel
    }

    fun setSelection(type: PreferenceType, value: Set<String>) {
        when (type) {
            PreferenceType.STYLES -> stylesSel = value
            PreferenceType.COLORS -> colorsSel = value
            PreferenceType.SIZES -> sizesSel = value
            PreferenceType.CATEGORIES -> categoriesSel = value
        }
    }

    // Mapa final para onContinue
    val selectionsMap = remember(stylesSel, colorsSel, sizesSel, categoriesSel) {
        mapOf(
            PreferenceType.STYLES to stylesSel,
            PreferenceType.COLORS to colorsSel,
            PreferenceType.SIZES to sizesSel,
            PreferenceType.CATEGORIES to categoriesSel
        )
    }

    // Progreso: categorías completadas / total
    val completedCount = remember(stylesSel, colorsSel, sizesSel, categoriesSel) {
        listOf(stylesSel, colorsSel, sizesSel, categoriesSel).count { it.isNotEmpty() }
    }
    val progress = completedCount / total.toFloat()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // Usa UNA de las dos líneas según tu versión de Material3:
                // LinearProgressIndicator(progress = { progress })   // versiones nuevas
                LinearProgressIndicator(
                progress = { progress },
                    color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )          // versiones que esperan Float
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Completado: $completedCount de $total categorías",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            PreferencesScreen(
                preferenceType = currentType,
                onBackClick = {
                    if (stepIndex == 0) onBackToRegister() else stepIndex--
                },
                onSkipClick = {
                    // Bloqueamos “Saltar”
                    scope.launch {
                        snackbarHostState.showSnackbar("Debes seleccionar al menos una opción por categoría.")
                    }
                },
                onContinueClick = { selectedThisStep ->
                    // 1) Guardar selección del paso actual
                    setSelection(currentType, selectedThisStep)

                    // 2) Buscar la siguiente categoría que aún NO tenga selección
                    val nextPendingType = steps.firstOrNull { getSelection(it).isEmpty() }

                    if (nextPendingType != null) {
                        // Ir a esa categoría pendiente (aunque no sea la siguiente lineal)
                        stepIndex = steps.indexOf(nextPendingType)
                    } else {
                        // 3) Si no hay pendientes, terminamos el flujo
                        onFinishClick(selectionsMap)
                    }
                }
            )
        }
    }
}



@Preview(showBackground = true, name = "Preferences – Colors – Dark")
@Composable
private fun PreferencesPreviewDark() {
    FitMatchTheme(darkTheme = true, dynamicColor = false) {
        PreferencesScreen(preferenceType = PreferenceType.COLORS)
    }
}
