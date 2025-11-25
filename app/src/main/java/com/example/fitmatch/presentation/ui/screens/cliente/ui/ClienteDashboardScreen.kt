package com.example.fitmatch.presentation.ui.screens.cliente

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitmatch.presentation.ui.screens.cliente.state.ProductCardState
import com.example.fitmatch.presentation.viewmodel.user.ClienteDashboardViewModel
import com.example.fitmatch.presentation.viewmodel.user.DashboardEvent
import kotlinx.coroutines.launch

/**
 * Dashboard de Cliente con UDF y Sensor de Inclinación.
 * - Swipe manual para like/pass
 * - Sensor de inclinación (tilt) para control hands-free
 * - Animaciones fluidas y feedback visual
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteDashboardScreen(
    onBackClick: () -> Unit = {},
    onOpenStore: (ProductCardState) -> Unit = {},
    onFilterClick: () -> Unit = {},
    onProductSeen: (ProductCardState) -> Unit = {},
    onProductLiked: (ProductCardState) -> Unit = {},
    onProductPassed: (ProductCardState) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    // ViewModel con contexto para el sensor
    val viewModel: ClienteDashboardViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ClienteDashboardViewModel(context) as T
            }
        }
    )

    // ========== OBSERVAR ESTADO ==========
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ========== MANEJAR EVENTOS ==========
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.ProductLiked -> {
                    onProductLiked(event.product)
                }
                is DashboardEvent.ProductPassed -> {
                    onProductPassed(event.product)
                }
                is DashboardEvent.NavigateToStore -> {
                    // TODO: Navegar a tienda
                }
                is DashboardEvent.ShowProductDetails -> {
                    // Manejar en el composable con bottom sheet
                }
                is DashboardEvent.ShowTiltFeedback -> {
                    snackbarHostState.showSnackbar(
                        message = "📱 ${event.action}",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        viewModel.startTemperatureSensor()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        viewModel.stopTemperatureSensor()
                    }
                    else -> {}
                }
            }
        )
    }


    // Bottom Sheet para detalles del producto
    var selectedProduct by remember { mutableStateOf<ProductCardState?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val temperature by viewModel.temperature.collectAsState(initial = 15f)

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
                    // 🔹 Izquierda: Botón de volver + temperatura
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = colors.onSurface
                            )
                        }

                        // Círculo de temperatura
                        if (temperature != null) {
                            val tempColor = when {
                                temperature!! < 15 -> Color(0xFF2196F3) // Azul - frío
                                temperature!! < 25 -> Color(0xFF4CAF50) // Verde - templado
                                else -> Color(0xFFFF7043) // Naranja/rojo - calor
                            }

                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = tempColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, tempColor)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "${temperature?.toInt()}°",
                                        color = tempColor,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 🔹 Título centrado
                    Text(
                        text = "Descubrir",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = colors.onSurface
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // 🔹 Derecha: sensores y filtro
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.onToggleTiltSensor(!uiState.isTiltEnabled)
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.isTiltEnabled)
                                    Icons.Filled.PhoneAndroid
                                else
                                    Icons.Filled.PhoneIphone,
                                contentDescription = "Sensor de inclinación",
                                tint = if (uiState.isTiltEnabled)
                                    colors.primary
                                else
                                    colors.onSurface
                            )
                        }

                        IconButton(onClick = onFilterClick) {
                            Icon(
                                imageVector = Icons.Filled.FilterAlt,
                                contentDescription = "Filtrar",
                                tint = colors.onSurface
                            )
                        }
                    }
                }
            }
        }
    )
    { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Instrucciones de Tilt (si están activas)
                AnimatedVisibility(
                    visible = uiState.showTiltInstructions,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    TiltInstructionsCard(
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Deck de productos
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    } else if (!uiState.hasProducts) {
                        EmptyDiscoverState(
                            onReset = { viewModel.onReloadProducts() }
                        )
                    } else {
                        ProductDeck(
                            products = uiState.productDeck,
                            onProductSeen = { viewModel.onProductSeen(it) },
                            onSwiped = { product, isLike ->
                                if (isLike) viewModel.onSwipeRight()
                                else viewModel.onSwipeLeft()
                            },
                            onViewDetails = { selectedProduct = it },
                            onSaveToggle = { viewModel.onToggleSave(it.id) }
                        )
                    }
                }

                // Action Bar
                ActionBar(
                    enabled = uiState.currentProduct != null,
                    isSaved = uiState.currentProduct?.isSaved == true,
                    canUndo = uiState.canUndo,
                    onPass = { viewModel.onSwipeLeft() },
                    onUndo = { viewModel.onUndo() },
                    onViewDetails = {
                        uiState.currentProduct?.let { selectedProduct = it }
                    },
                    onSave = {
                        uiState.currentProduct?.let { viewModel.onToggleSave(it.id) }
                    },
                    onLike = { viewModel.onSwipeRight() },
                    onQuickShop = {
                        uiState.currentProduct?.let { viewModel.onOpenStore(it) }
                    }
                )
            }
        }

        // Bottom Sheet de detalles
        if (selectedProduct != null) {
            ProductDetailSheet(
                product = selectedProduct!!,
                sheetState = sheetState,
                onDismiss = { selectedProduct = null },
                onOpenStore = { product ->
                    viewModel.onOpenStore(product)
                    selectedProduct = null
                },
                onSaveToggle = { product, _ ->
                    viewModel.onToggleSave(product.id)
                }
            )
        }
    }
}

/* ==================== TARJETA DE INSTRUCCIONES ==================== */

@Composable
private fun TiltInstructionsCard(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PhoneAndroid,
                contentDescription = null,
                tint = colors.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Modo Inclinación Activado",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer
                )
                Text(
                    text = "Inclina tu teléfono: ← Pass | → Like",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onPrimaryContainer
                )
            }
        }
    }
}

/* ==================== DECK DE PRODUCTOS ==================== */

@Composable
private fun ProductDeck(
    products: List<ProductCardState>,
    onProductSeen: (ProductCardState) -> Unit,
    onSwiped: (ProductCardState, isLike: Boolean) -> Unit,
    onViewDetails: (ProductCardState) -> Unit,
    onSaveToggle: (ProductCardState) -> Unit
) {
    val visibleProducts = remember(products.size) { products.takeLast(3) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        visibleProducts.forEachIndexed { index, product ->
            val isTopCard = index == visibleProducts.lastIndex
            val zIndex = if (isTopCard) 3f else (index + 1).toFloat()

            // Efecto de profundidad (3D stack)
            val scale = if (isTopCard) 1f else 0.95f - (0.03f * (visibleProducts.lastIndex - index))
            val yOffset = if (isTopCard) 0.dp else (8 * (visibleProducts.lastIndex - index)).dp
            val alpha = if (isTopCard) 1f else 0.6f - (0.1f * (visibleProducts.lastIndex - index))

            SwipeableProductCard(
                product = product,
                isTopCard = isTopCard,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(0.7f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .offset(y = yOffset)
                    .zIndex(zIndex),
                onProductSeen = onProductSeen,
                onViewDetails = { onViewDetails(product) },
                onSaveToggle = { onSaveToggle(product) },
                onSwiped = { isLike -> onSwiped(product, isLike) }
            )
        }
    }
}

/* ==================== TARJETA SWIPEABLE ==================== */

@Composable
private fun SwipeableProductCard(
    product: ProductCardState,
    isTopCard: Boolean,
    modifier: Modifier = Modifier,
    onProductSeen: (ProductCardState) -> Unit,
    onViewDetails: () -> Unit,
    onSaveToggle: () -> Unit,
    onSwiped: (isLike: Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var cardSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Animación del offset de la tarjeta
    val offset = remember(product.id) {
        Animatable(Offset.Zero, Offset.VectorConverter)
    }

    LaunchedEffect(product.id) {
        onProductSeen(product)
    }

    val swipeThreshold = with(density) { 120.dp.toPx() }

    Card(
        modifier = modifier
            .onGloballyPositioned { cardSize = it.size }
            .then(
                if (isTopCard) {
                    Modifier.pointerInput(product.id) {
                        detectDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    when {
                                        offset.value.x > swipeThreshold ->
                                            animateCardExit(true, cardSize, offset, onSwiped)
                                        offset.value.x < -swipeThreshold ->
                                            animateCardExit(false, cardSize, offset, onSwiped)
                                        else ->
                                            resetCardPosition(offset)
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch { resetCardPosition(offset) }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offset.snapTo(offset.value + dragAmount)
                            }
                        }
                    }
                } else Modifier
            )
            .offset {
                androidx.compose.ui.unit.IntOffset(
                    offset.value.x.toInt(),
                    offset.value.y.toInt()
                )
            }
            .rotate((offset.value.x / cardSize.width.coerceAtLeast(1)) * 10f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.15f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen / placeholder con gradiente
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.surfaceVariant,
                                colors.surface
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.ShoppingBag,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        "Imagen del producto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            // Gradiente inferior para texto legible
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                colors.surface.copy(alpha = 0.95f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Información del producto
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Text(
                    product.brand,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.primary
                )
                Text(
                    product.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    "${formatPrice(product.price)}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = colors.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Tags
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    product.tags.take(3).forEach { tag ->
                        ProductTag(tag = tag)
                    }
                }
            }

            // Botones de acción (guardar e info)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallActionButton(
                    icon = if (product.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    onClick = onSaveToggle,
                    tint = if (product.isSaved) colors.primary else colors.onSurface
                )
                SmallActionButton(
                    icon = Icons.Filled.Info,
                    onClick = onViewDetails
                )
            }

            // Indicadores de swipe (LIKE y PASS) - MEJORADOS
            val likeAlpha = (offset.value.x / (swipeThreshold * 1.5f)).coerceIn(0f, 1f)
            val passAlpha = (-offset.value.x / (swipeThreshold * 1.5f)).coerceIn(0f, 1f)

            // Indicador LIKE (derecha)
            AnimatedSwipeIndicator(
                visibleAlpha = likeAlpha,
                label = "ME GUSTA",
                icon = Icons.Filled.Favorite,
                color = colors.primary,
                rotation = -15f,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp)
            )

            // Indicador PASS (izquierda)
            AnimatedSwipeIndicator(
                visibleAlpha = passAlpha,
                label = "PASS",
                icon = Icons.Filled.Close,
                color = colors.error,
                rotation = 15f,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
            )
        }
    }
}

/* ==================== INDICADOR DE SWIPE MEJORADO ==================== */

@Composable
private fun AnimatedSwipeIndicator(
    visibleAlpha: Float,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    rotation: Float,
    modifier: Modifier
) {
    if (visibleAlpha <= 0f) return

    // Animación de escala basada en el alpha
    val scale by animateFloatAsState(
        targetValue = 0.8f + (visibleAlpha * 0.4f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Surface(
        modifier = modifier
            .rotate(rotation)
            .graphicsLayer {
                this.alpha = visibleAlpha
                scaleX = scale
                scaleY = scale
            },
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(3.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black
                ),
                color = color,
                letterSpacing = 1.5.sp
            )
        }
    }
}

/* ==================== ACTION BAR ==================== */

@Composable
private fun ActionBar(
    enabled: Boolean,
    isSaved: Boolean,
    canUndo: Boolean,
    onPass: () -> Unit,
    onUndo: () -> Unit,
    onViewDetails: () -> Unit,
    onSave: () -> Unit,
    onLike: () -> Unit,
    onQuickShop: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Botones principales (grandes)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PASS
            ActionButton(
                icon = Icons.Filled.Close,
                contentDescription = "No me gusta",
                tint = colors.error,
                enabled = enabled,
                onClick = onPass,
                size = 64.dp
            )

            // SHOP
            ActionButton(
                icon = Icons.Filled.ShoppingCart,
                contentDescription = "Comprar ahora",
                tint = colors.primary,
                enabled = enabled,
                onClick = onQuickShop,
                size = 56.dp
            )

            // LIKE
            ActionButton(
                icon = Icons.Filled.Favorite,
                contentDescription = "Me gusta",
                tint = colors.primary,
                enabled = enabled,
                onClick = onLike,
                size = 64.dp
            )
        }

        // Botón de deshacer (centrado)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FilledTonalButton(
                onClick = onUndo,
                enabled = canUndo,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colors.secondaryContainer,
                    contentColor = colors.onSecondaryContainer
                )
            ) {
                Icon(
                    Icons.Filled.Replay,
                    contentDescription = "Deshacer",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Deshacer",
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    size: Dp = 56.dp
) {
    val colors = MaterialTheme.colorScheme

    // Animación de escala al presionar
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )

    Surface(
        onClick = {
            isPressed = true
            onClick()
            // Reset después de un frame
            isPressed = false
        },
        enabled = enabled,
        shape = CircleShape,
        color = colors.surface,
        tonalElevation = if (enabled) 8.dp else 0.dp,
        shadowElevation = if (enabled) 4.dp else 0.dp,
        border = BorderStroke(
            2.dp,
            if (enabled) tint.copy(alpha = 0.3f) else colors.outline.copy(alpha = 0.1f)
        ),
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = if (enabled) tint else tint.copy(alpha = 0.3f),
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

@Composable
private fun SmallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = colors.surface.copy(alpha = 0.9f),
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.2f))
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .padding(10.dp)
                .size(20.dp)
        )
    }
}

/* ==================== COMPONENTES AUXILIARES ==================== */

@Composable
private fun ProductTag(tag: String) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.primary.copy(alpha = 0.12f),
        contentColor = colors.primary
    ) {
        Text(
            tag,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun EmptyDiscoverState(onReset: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.SentimentDissatisfied,
            contentDescription = "Sin productos",
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(80.dp)
        )
        Text(
            "No hay más productos por ahora",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "Ajusta los filtros o vuelve más tarde",
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Button(
            onClick = onReset,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Recargar productos", fontWeight = FontWeight.SemiBold)
        }
    }
}

/* ==================== BOTTOM SHEET DE DETALLES ==================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailSheet(
    product: ProductCardState,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onOpenStore: (ProductCardState) -> Unit,
    onSaveToggle: (ProductCardState, Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = colors.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.brand,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.primary
                    )
                    Text(
                        product.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    "${formatPrice(product.price)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Especificaciones
            SpecificationRow(title = "Categoría", value = product.category)
            SpecificationRow(title = "Talla", value = product.size)
            SpecificationRow(title = "Color", value = product.color)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Características",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                product.tags.forEach { tag ->
                    ProductTag(tag = tag)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onOpenStore(product) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp)
                ) {
                    Icon(
                        Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Ir a la tienda",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                OutlinedButton(
                    onClick = { onSaveToggle(product, !product.isSaved) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    border = BorderStroke(1.dp, colors.outline)
                ) {
                    Icon(
                        if (product.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        if (product.isSaved) "Guardado" else "Guardar",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SpecificationRow(title: String, value: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

/* ==================== HELPERS Y ANIMACIONES ==================== */

private fun formatPrice(value: Int): String =
    "%,d".format(value).replace(',', '.')

private suspend fun resetCardPosition(offset: Animatable<Offset, *>) {
    offset.updateBounds(Offset.Zero, Offset.Zero)
    offset.animateTo(
        Offset.Zero,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
}

private suspend fun animateCardExit(
    toRight: Boolean,
    size: IntSize,
    offset: Animatable<Offset, *>,
    onSwiped: (isLike: Boolean) -> Unit
) {
    // Evita animar si el tamaño no está definido todavía
    if (size.width == 0 || size.height == 0) return

    val targetX = if (toRight) size.width * 1.5f else -size.width * 1.5f
    val targetY = -size.height * 0.2f

    try {
        // Usa límites válidos
        offset.updateBounds(
            lowerBound = Offset(-size.width.toFloat() * 2, -size.height.toFloat() * 2),
            upperBound = Offset(size.width.toFloat() * 2, size.height.toFloat() * 2)
        )

        // Ejecuta la animación
        offset.animateTo(
            Offset(targetX, targetY),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )

        onSwiped(toRight)
        offset.snapTo(Offset.Zero)
    } catch (e: Exception) {
        Log.e("ClienteDashboard", "Error en animación: ${e.message}")
    }
}
