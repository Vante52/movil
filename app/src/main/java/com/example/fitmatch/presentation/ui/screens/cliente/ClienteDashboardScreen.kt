package com.example.fitmatch.presentation.ui.screens.cliente

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.compose.FitMatchTheme
import kotlinx.coroutines.launch


/* Model / Mock data  */

data class ProductCard(
    val id: String,
    val title: String,
    val brand: String,
    val price: Int,
    val imageUrl: String = "",
    val category: String = "",
    val size: String = "",
    val color: String = "",
    val tags: List<String> = emptyList(),
    val isSaved: Boolean = false,
    val storeUrl: String = ""
)

private fun mockProducts(): List<ProductCard> = listOf(
    ProductCard("p1", "Blazer Premium en Lino", "Atelier Nova", 189900,
        tags = listOf("Formal", "Oficina", "Lino"), category = "Abrigos", size = "M", color = "Beige"),
    ProductCard("p2", "Pantalón Wide Leg", "Luna Urban", 129900,
        tags = listOf("Casual", "Comfort", "Algodón"), category = "Pantalones", size = "38", color = "Negro"),
    ProductCard("p3", "Camisa Oversize", "Forastera", 99900,
        tags = listOf("Street", "Oversize", "Algodón"), category = "Camisas", size = "L", color = "Blanco"),
    ProductCard("p4", "Chaqueta Denim Vintage", "Retro Club", 159900,
        tags = listOf("Vintage", "Denim", "Otoño"), category = "Chaquetas", size = "S", color = "Azul"),
    ProductCard("p5", "Vestido Midi Floral", "Marea", 139900,
        tags = listOf("Primavera", "Floral", "Elegante"), category = "Vestidos", size = "XS", color = "Rosa"),
)

/* --------------------------------- Screen --------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteDashboardScreen(
    onBackClick: () -> Unit = {},
    onOpenStore: (ProductCard) -> Unit = {},
    onFilterClick: () ->Unit ={},
    onProductSeen: (ProductCard) -> Unit = {},
    onProductLiked: (ProductCard) -> Unit = {},
    onProductPassed: (ProductCard) -> Unit = {},
    onSaveToggle: (ProductCard, Boolean) -> Unit = { _, _ -> },
) {
    val colors = MaterialTheme.colorScheme

    val productDeck = remember { mutableStateListOf<ProductCard>().apply { addAll(mockProducts()) } }

    data class SwipeAction(val product: ProductCard, val action: String)
    val history = remember { mutableStateListOf<SwipeAction>() }

    var selectedCategory by rememberSaveable { mutableStateOf("Todos") }
    var priceRange by rememberSaveable { mutableStateOf("Cualquier precio") }

    var selectedProduct by remember { mutableStateOf<ProductCard?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = colors.background,
        topBar = {
            // Header unificado: título centrado + back + acción "+"
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
                            contentDescription = "Volver",
                            tint = colors.onSurface
                        )
                    }

                    // Título centrado (misma tipografía)
                    Text(
                        text = "Descubrir",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = colors.onSurface
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Acción: agregar categoría (+)
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (productDeck.isEmpty()) {
                    EmptyDiscoverState(onReset = {
                        productDeck.addAll(mockProducts())
                        history.clear()
                    })
                } else {
                    ProductDeck(
                        products = productDeck,
                        onProductSeen = onProductSeen,
                        onSwiped = { product, action ->
                            productDeck.remove(product)
                            history.add(SwipeAction(product, action))
                            when (action) {
                                "like" -> onProductLiked(product)
                                "pass" -> onProductPassed(product)
                            }
                        },
                        onViewDetails = { selectedProduct = it },
                        onSaveToggle = { product ->
                            val index = productDeck.indexOfFirst { it.id == product.id }
                            if (index >= 0) {
                                val updated = productDeck[index].copy(isSaved = !productDeck[index].isSaved)
                                productDeck[index] = updated
                                onSaveToggle(updated, updated.isSaved)
                            }
                        }
                    )
                }
            }

            val currentProduct = productDeck.lastOrNull()
            ActionBar(
                enabled = currentProduct != null,
                isSaved = currentProduct?.isSaved == true,
                onPass = { currentProduct?.let { SwipeActionBus.requestSwipeLeft++ } },
                onUndo = {
                    val lastAction = history.removeLastOrNull()
                    lastAction?.let { productDeck.add(it.product) }
                },
                onViewDetails = { currentProduct?.let { selectedProduct = it } },
                onSave = {
                    currentProduct?.let {
                        val index = productDeck.indexOfLast { product -> productDeck.lastOrNull()?.id == it.id }
                        if (index >= 0) {
                            val updated = productDeck[index].copy(isSaved = !productDeck[index].isSaved)
                            productDeck[index] = updated
                            onSaveToggle(updated, updated.isSaved)
                        }
                    }
                },
                onLike = { currentProduct?.let { SwipeActionBus.requestSwipeRight++ } },
                onQuickShop = { currentProduct?.let { onOpenStore(it) } }
            )
        }

        if (selectedProduct != null) {
            ProductDetailSheet(
                product = selectedProduct!!,
                sheetState = sheetState,
                onDismiss = { selectedProduct = null },
                onOpenStore = { product ->
                    onOpenStore(product)
                    selectedProduct = null
                },
                onSaveToggle = { product, saved ->
                    onSaveToggle(product, saved)
                    val index = productDeck.indexOfFirst { it.id == product.id }
                    if (index >= 0) productDeck[index] = product.copy(isSaved = saved)
                }
            )
        }
    }
}

/* ------------------------------ Filtros ----------------------------------- */

@Composable
private fun CategoryFilterRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    priceRange: String,
    onPriceRangeSelected: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val categories = listOf("Todos", "Abrigos", "Camisas", "Pantalones", "Vestidos", "Accesorios")
    val priceRanges = listOf("Cualquier precio", "Menos de $100K", "$100K-$200K", "Más de $200K")

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            text = "Categorías",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    leadingIcon = if (selectedCategory == category) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.outline),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primaryContainer,
                        selectedLabelColor = colors.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Rango de precio",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(priceRanges) { range ->
                FilterChip(
                    selected = priceRange == range,
                    onClick = { onPriceRangeSelected(range) },
                    label = { Text(range) },
                    leadingIcon = if (priceRange == range) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.outline),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primaryContainer,
                        selectedLabelColor = colors.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/* --------------------------------- Deck ----------------------------------- */

private object SwipeActionBus { var requestSwipeLeft by mutableStateOf(0); var requestSwipeRight by mutableStateOf(0) }

@Composable
private fun ProductDeck(
    products: List<ProductCard>,
    onProductSeen: (ProductCard) -> Unit,
    onSwiped: (ProductCard, action: String) -> Unit,
    onViewDetails: (ProductCard) -> Unit,
    onSaveToggle: (ProductCard) -> Unit,
) {
    val visibleProducts = remember(products.size) { products.takeLast(3) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        visibleProducts.forEachIndexed { index, product ->
            val isTopCard = index == visibleProducts.lastIndex
            val zIndex = if (isTopCard) 3f else (index + 1).toFloat()
            val scale = if (isTopCard) 1f else 0.95f - (0.02f * (visibleProducts.lastIndex - index))
            val yOffset = if (isTopCard) 0.dp else (6 * (visibleProducts.lastIndex - index)).dp

            SwipeableProductCard(
                product = product,
                isTopCard = isTopCard,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(0.7f)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .offset(y = yOffset)
                    .zIndex(zIndex),
                onProductSeen = onProductSeen,
                onViewDetails = { onViewDetails(product) },
                onSaveToggle = { onSaveToggle(product) },
                onSwiped = { action -> onSwiped(product, action) }
            )
        }
    }
}

@Composable
private fun SwipeableProductCard(
    product: ProductCard,
    isTopCard: Boolean,
    modifier: Modifier = Modifier,
    onProductSeen: (ProductCard) -> Unit,
    onViewDetails: () -> Unit,
    onSaveToggle: () -> Unit,
    onSwiped: (action: String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    var cardSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val offset = remember(product.id) { Animatable(Offset.Zero, Offset.VectorConverter) }

    LaunchedEffect(product.id) { onProductSeen(product) }

    val leftSwipeSignal = SwipeActionBus.requestSwipeLeft
    val rightSwipeSignal = SwipeActionBus.requestSwipeRight
    LaunchedEffect(isTopCard, leftSwipeSignal, rightSwipeSignal) {
        if (!isTopCard) return@LaunchedEffect
        when {
            leftSwipeSignal > 0 -> { SwipeActionBus.requestSwipeLeft = 0; animateCardExit(false, cardSize, offset, onSwiped) }
            rightSwipeSignal > 0 -> { SwipeActionBus.requestSwipeRight = 0; animateCardExit(true, cardSize, offset, onSwiped) }
        }
    }

    val swipeThreshold = with(density) { 100.dp.toPx() }

    Card(
        modifier = modifier
            .onGloballyPositioned { cardSize = it.size }
            .then(
                if (isTopCard) Modifier.pointerInput(product.id) {
                    detectDragGestures(
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offset.value.x > swipeThreshold -> animateCardExit(true, cardSize, offset, onSwiped)
                                    offset.value.x < -swipeThreshold -> animateCardExit(false, cardSize, offset, onSwiped)
                                    else -> resetCardPosition(offset)
                                }
                            }
                        },
                        onDragCancel = { scope.launch { resetCardPosition(offset) } }
                    ) { change, dragAmount ->
                        change.consume()
                        scope.launch { offset.snapTo(offset.value + dragAmount) }
                    }
                } else Modifier
            )
            .offset { androidx.compose.ui.unit.IntOffset(offset.value.x.toInt(), offset.value.y.toInt()) }
            .rotate((offset.value.x / (cardSize.width.coerceAtLeast(1))) * 8f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen / placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(colors.surfaceVariant, colors.surface),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.ShoppingBag, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(64.dp))
                    Text("Imagen del producto", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                }
            }

            // Gradiente inferior para legibilidad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, colors.surface.copy(alpha = 0.9f)),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
            ) {
                Text(product.brand, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = colors.primary)
                Text(product.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.onSurface, modifier = Modifier.padding(top = 4.dp))
                Text("$${formatPrice(product.price)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = colors.onSurface, modifier = Modifier.padding(top = 8.dp))
                // Etiquetas (usa FlowRow a partir de foundation)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    product.tags.take(2).forEach { tag -> ProductTag(tag = tag) }
                }
            }

            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallActionButton(icon = if (product.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, onClick = onSaveToggle)
                SmallActionButton(icon = Icons.Filled.Info, onClick = onViewDetails)
            }

            val likeAlpha = (offset.value.x / (swipeThreshold * 2)).coerceIn(0f, 1f)
            val passAlpha = (-offset.value.x / (swipeThreshold * 2)).coerceIn(0f, 1f)

            SwipeIndicator(visibleAlpha = likeAlpha, isLike = true, modifier = Modifier.align(Alignment.TopStart).padding(20.dp))
            SwipeIndicator(visibleAlpha = passAlpha, isLike = false, modifier = Modifier.align(Alignment.TopEnd).padding(20.dp))
        }
    }
}

@Composable
private fun SwipeIndicator(visibleAlpha: Float, isLike: Boolean, modifier: Modifier) {
    if (visibleAlpha <= 0f) return
    val colors = MaterialTheme.colorScheme
    val label = if (isLike) "ME GUSTA" else "PASS"
    val color = if (isLike) colors.primary else colors.error

    Surface(
        modifier = modifier.rotate(if (isLike) -12f else 12f),
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(3.dp, color)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).graphicsLayer { alpha = visibleAlpha },
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
            color = color,
            letterSpacing = 1.sp
        )
    }
}

/* ------------------------------ Action Bar -------------------------------- */

@Composable
private fun ActionBar(
    enabled: Boolean,
    isSaved: Boolean,
    onPass: () -> Unit,
    onUndo: () -> Unit,
    onViewDetails: () -> Unit,
    onSave: () -> Unit,
    onLike: () -> Unit,
    onQuickShop: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(icon = Icons.Filled.Close, contentDescription = "No me gusta", tint = colors.error, enabled = enabled, onClick = onPass, size = 56.dp)
        ActionButton(icon = Icons.Filled.ShoppingCart, contentDescription = "Comprar ahora", tint = colors.primary, enabled = enabled, onClick = onQuickShop, size = 56.dp)
        ActionButton(icon = Icons.Filled.Favorite, contentDescription = "Me gusta", tint = colors.primary, enabled = enabled, onClick = onLike, size = 56.dp)
    }

    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.Center) {
        FilledTonalButton(onClick = onUndo, shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Filled.Replay, contentDescription = "Deshacer", modifier = Modifier.size(20.dp))
            Text("Deshacer", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean,
    onClick: () -> Unit,
    size: Dp = 48.dp
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = colors.surface,
        tonalElevation = if (enabled) 4.dp else 0.dp,
        shadowElevation = if (enabled) 2.dp else 0.dp,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = if (enabled) 0.2f else 0.1f))
    ) {
        Icon(icon, contentDescription = contentDescription, tint = if (enabled) tint else tint.copy(alpha = 0.3f), modifier = Modifier.padding(12.dp).size(size))
    }
}

@Composable
private fun SmallActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(onClick = onClick, shape = CircleShape, color = colors.surface, tonalElevation = 4.dp, border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.2f))) {
        Icon(icon, contentDescription = null, tint = colors.onSurface, modifier = Modifier.padding(10.dp).size(20.dp))
    }
}

/* --------------------------------- Etiquetas --------------------------------- */

@Composable
private fun ProductTag(tag: String) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(12.dp), color = colors.primary.copy(alpha = 0.1f), contentColor = colors.primary) {
        Text(tag, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium))
    }
}

/* ------------------------------- Estado Vacío ------------------------------ */

@Composable
private fun EmptyDiscoverState(onReset: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.SentimentDissatisfied, contentDescription = "Sin productos", tint = colors.onSurfaceVariant, modifier = Modifier.size(64.dp))
        Text("No hay más productos por ahora", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
        Text("Ajusta los filtros o vuelve más tarde", color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        Button(onClick = onReset, shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(top = 24.dp)) { Text("Recargar productos", fontWeight = FontWeight.SemiBold) }
    }
}

/* ----------------------------- Bottom Sheet ------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailSheet(
    product: ProductCard,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onOpenStore: (ProductCard) -> Unit,
    onSaveToggle: (ProductCard, Boolean) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, dragHandle = { BottomSheetDefaults.DragHandle() }, containerColor = colors.surface, tonalElevation = 8.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.brand, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = colors.primary)
                    Text(product.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.onSurface, modifier = Modifier.padding(top = 4.dp))
                }
                Text("$${formatPrice(product.price)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = colors.onSurface)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                SpecificationRow(title = "Categoría", value = product.category)
                SpecificationRow(title = "Talla", value = product.size)
                SpecificationRow(title = "Color", value = product.color)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Características", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                product.tags.forEach { tag -> ProductTag(tag = tag) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { onOpenStore(product) }, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1.5f).height(56.dp)) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Ir a la tienda", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(onClick = { onSaveToggle(product, !product.isSaved) }, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f).height(56.dp), border = BorderStroke(1.dp, colors.outline)) {
                    Icon(if (product.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(if (product.isSaved) "Guardado" else "Guardar", modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SpecificationRow(title: String, value: String) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface, fontWeight = FontWeight.Medium)
    }
}

/* ----------------------------- Helpers/Animations ------------------------- */

private fun formatPrice(value: Int): String = "% ,d".replace(" ", "").let { String.format("%,d", value) }.replace(',', '.')

private suspend fun resetCardPosition(offset: Animatable<Offset, *>) {
    offset.updateBounds(Offset.Zero, Offset.Zero)
    offset.animateTo(Offset.Zero, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
}

private suspend fun animateCardExit(toRight: Boolean, size: IntSize, offset: Animatable<Offset, *>, onSwiped: (String) -> Unit) {
    val targetX = if (toRight) size.width * 1.5f else -size.width * 1.5f
    offset.updateBounds(Offset.Unspecified, Offset.Unspecified)
    offset.animateTo(Offset(targetX, 0f), animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
    onSwiped(if (toRight) "like" else "pass")
    offset.snapTo(Offset.Zero)
}

/* --------------------------------- Preview -------------------------------- */

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_6")
@Composable
private fun DiscoverScreenPreview() {
    FitMatchTheme(dynamicColor = false) { ClienteDashboardScreen () }
}
