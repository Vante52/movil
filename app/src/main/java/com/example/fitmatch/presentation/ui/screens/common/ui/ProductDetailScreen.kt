package com.example.fitmatch.presentation.ui.screens.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.compose.FitMatchTheme
import com.example.fitmatch.presentation.ui.screens.cliente.state.ProductCardState

// ────────────────────────────────────────────────────────────────────────────────
// DATA
// ────────────────────────────────────────────────────────────────────────────────

data class ProductDetail(
    val title: String,
    val originalPrice: String,
    val currentPrice: String,
    val isOnSale: Boolean,
    val brand: String,
    val size: String,
    val category: String,
    val condition: String,
    val color: String,
    val mascotMessage: String,
    val sellerName: String,
    val sellerImage: String? = null
)

data class DetailSection(
    val title: String,
    val icon: ImageVector,
    val isExpandable: Boolean = false,
    val content: String = ""
)

data class ProductComment(val user: String, val text: String)

data class ProductSocial(
    val likes: Int,
    val comments: List<ProductComment>,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val postedAgo: String = "hace 2 h",
    val caption: String = "" // breve descripción tipo IG
)

// ────────────────────────────────────────────────────────────────────────────────
// SCREEN
// ────────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: ProductCardState? = null,
    onBackClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onBuyClick: () -> Unit = {},
    onOpenComments: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    val productDetail = product?.let {
        val formattedPrice = "$${formatPrice(it.price)}"
        ProductDetail(
            title = it.title,
            originalPrice = formattedPrice,
            currentPrice = formattedPrice,
            isOnSale = false,
            brand = it.brand.ifBlank { "Vendedor" },
            size = it.size.ifBlank { "Talla única" },
            category = it.category.ifBlank { "Sin categoría" },
            condition = it.description.ifBlank { "Sin descripción" },
            color = it.color.ifBlank { "Sin color" },
            mascotMessage = "Descubre más detalles de este producto",
            sellerName = it.brand.ifBlank { "Vendedor" }
        )
    } ?: ProductDetail(
        title = "Pantalón Blanco",
        originalPrice = "$380,000",
        currentPrice = "$300,000",
        isOnSale = true,
        brand = "Levi's",
        size = "XS / 30 US / 42 EU",
        category = "Jeans",
        condition = "En perfecto estado",
        color = "Blanco",
        mascotMessage = "Tito cree que esta prenda es perfecta para tu estilo ✨",
        sellerName = "Planeta Vintage"
    )

    val social = remember(product) {
        ProductSocial(
            likes = 120,
            comments = listOf(
                ProductComment("val_usershop", "¡Se ve brutal! ¿Tienes en S?"),
                ProductComment("andrea.v", "Lo compré, la tela es divina 💖"),
                ProductComment("joseph", "¿Hay entrega hoy en Bogotá?")
            ),
            caption = product?.description.takeUnless { it.isNullOrBlank() }
                ?: "Fit relajado, tiro medio. Ideal para street outfits."        )
    }
    val mainImageUrl = remember(product) {
        product?.imageUrls?.firstOrNull { it.isNotBlank() }
            ?: product?.imageUrl
            ?: ""
    }

    val detailSections = listOf(
        DetailSection("Descripción y detalles", Icons.Default.Description),
        DetailSection("Talla", Icons.Default.Straighten),
        DetailSection("Categoría", Icons.Default.Category),
        DetailSection("Estado", Icons.Default.Star),
        DetailSection("Color", Icons.Default.Palette),
        DetailSection("Guía de Tallas", Icons.Default.Straighten, isExpandable = true),
        DetailSection("Materiales", Icons.Default.Texture, isExpandable = true),
        DetailSection("Estimación de envío", Icons.Default.LocalShipping, isExpandable = true),
        DetailSection("Políticas de Protección", Icons.Default.Security, isExpandable = true)
    )

    // Estado social local (like/bookmark/likes)
    var liked by remember { mutableStateOf(social.isLiked) }
    var bookmarked by remember { mutableStateOf(social.isBookmarked) }
    var likeCount by remember { mutableStateOf(social.likes) }
    var captionExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.background,
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
                    IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = colors.onSurface)
                    }
                    Text(
                        text = "Detalles",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = colors.onSurface
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Row(modifier = Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onMoreClick) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir", tint = colors.onSurface)
                        }
                        IconButton(onClick = onMoreClick) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = colors.onSurface)
                        }
                    }
                }
            }
        }
    ) { inner ->
        Box(modifier = Modifier.fillMaxSize().padding(inner)) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                // Galería / imagen principal
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mainImageUrl.isNotBlank()) {
                            AsyncImage(
                                model = mainImageUrl,
                                contentDescription = productDetail.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("Imagen del Producto", color = colors.onSurfaceVariant, fontSize = 16.sp)
                        }
                    }
                }

                // —— NUEVO: bloque social al estilo Instagram ——
                item {
                    // Fila de acciones
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            liked = !liked
                            likeCount = if (liked) likeCount + 1 else (likeCount - 1).coerceAtLeast(0)
                        }) {
                            Icon(
                                imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (liked) "Quitar me gusta" else "Dar me gusta",
                                tint = if (liked) colors.error else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = onOpenComments) {
                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comentarios")
                        }
                        IconButton(onClick = { /* compartir/enviar */ }) {
                            Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Enviar")
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { bookmarked = !bookmarked }) {
                            Icon(
                                imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (bookmarked) "Guardado" else "Guardar"
                            )
                        }
                    }

                    // Likes
                    Text(
                        text = "$likeCount Me gusta",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    // Caption con “ver más”
                    val captionText = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(productDetail.brand)
                        }
                        append("  ")
                        append("${productDetail.title} — ${social.caption}")
                    }
                    Text(
                        text = captionText,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { captionExpanded = !captionExpanded },
                        maxLines = if (captionExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp
                    )
                    if (!captionExpanded) {
                        TextButton(
                            onClick = { captionExpanded = true },
                            contentPadding = PaddingValues(horizontal = 16.dp),
                        ) { Text("Más") }
                    }

                    // Vista previa de comentarios (2)
                    val preview = social.comments.take(2)
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        preview.forEach { c ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    buildAnnotatedString {
                                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(c.user) }
                                        append("  ${c.text}")
                                    },
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                        }
                    }

                    // Ver todos los comentarios
                    if (social.comments.isNotEmpty()) {
                        TextButton(
                            onClick = onOpenComments,
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) { Text("Ver los ${social.comments.size} comentarios") }
                    }

                    // Timestamp estilo IG
                    Text(
                        text = social.postedAgo.uppercase(),
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 8.dp)
                    )
                }
                // —— FIN bloque social ——

                // Título principal (conservado)
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        Text(productDetail.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    }
                }

                // Card vendedor/precios/mascota (conservada)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Colección René Risco", fontSize = 12.sp, color = colors.onSurfaceVariant)
                            Text(productDetail.sellerName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                            Text("Cerca: 9.6k • ${productDetail.condition}", fontSize = 12.sp, color = colors.onSurfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(productDetail.currentPrice, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                                if (productDetail.isOnSale) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(productDetail.originalPrice, fontSize = 14.sp, color = colors.onSurfaceVariant, textDecoration = TextDecoration.LineThrough)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) { Text("🐶", fontSize = 20.sp) }
                                Spacer(Modifier.width(12.dp))
                                Text(productDetail.mascotMessage, fontSize = 14.sp, color = colors.primary, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Secciones de detalle (conservadas)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val items = listOf(
                                DetailSection("Descripción y detalles", Icons.Default.Description),
                                DetailSection("Talla", Icons.Default.Straighten),
                                DetailSection("Categoría", Icons.Default.Category),
                                DetailSection("Estado", Icons.Default.Star),
                                DetailSection("Color", Icons.Default.Palette),
                                DetailSection("Guía de Tallas", Icons.Default.Straighten, isExpandable = true),
                                DetailSection("Materiales", Icons.Default.Texture, isExpandable = true),
                                DetailSection("Estimación de envío", Icons.Default.LocalShipping, isExpandable = true),
                                DetailSection("Políticas de Protección", Icons.Default.Security, isExpandable = true)
                            )
                            items.forEachIndexed { index, section ->
                                DetailSectionItem(section = section, productDetail = productDetail)
                                if (index < items.size - 1) {
                                    HorizontalDivider(color = colors.outlineVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }

            // Botón Comprar (conservado)
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                color = Color.Transparent
            ) {
                Button(
                    onClick = onBuyClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Comprar", color = colors.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────

@Composable
private fun DetailSectionItem(
    section: DetailSection,
    productDetail: ProductDetail
) {
    val colors = MaterialTheme.colorScheme
    val content = when (section.title) {
        "Descripción y detalles" -> "Prenda original, sin manchas ni roturas. Corte recto, tiro medio."
        "Talla" -> productDetail.size
        "Categoría" -> productDetail.category
        "Estado" -> productDetail.condition
        "Color" -> productDetail.color
        else -> ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = section.isExpandable) { /* TODO: expandir/cerrar */ }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(section.icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(section.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
            if (content.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(content, fontSize = 12.sp, color = colors.onSurfaceVariant)
            }
        }
        if (section.isExpandable) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Expandir", tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}
private fun formatPrice(value: Int): String =
    String.format("%,d", value).replace(',', '.')


// ────────────────────────────────────────────────────────────────────────────────
// PREVIEWS
// ────────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Product Detail – Light")
@Composable
private fun ProductDetailPreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        ProductDetailScreen()
    }
}

@Preview(showBackground = true, name = "Product Detail – Dark")
@Composable
private fun ProductDetailPreviewDark() {
    FitMatchTheme(darkTheme = true, dynamicColor = false) {
        ProductDetailScreen()
    }
}
