package com.example.fitmatch.presentation.ui.screens.cliente.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.FitMatchTheme
import com.example.fitmatch.presentation.viewmodel.user.ProfileViewModel

data class ProfileSection(
    val title: String,
    val items: List<ProfileItem>
)

data class ProfileItem(
    val label: String,
    val value: String,
    val icon: ImageVector? = null,
    val isEditable: Boolean = true,
    val onClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    onSavedClick: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val colors = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

    // Cargar datos del usuario al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // Manejar el logout exitoso
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogoutClick()
        }
    }

    var selectedSizes by remember { mutableStateOf(setOf("S", "M", "L")) }

    // Construir secciones con datos del usuario
    val personalInfo = ProfileSection(
        title = "INFORMACIÓN PERSONAL",
        items = buildList {
            add(ProfileItem("NOMBRE", uiState.user?.fullName ?: "Cargando..."))
            add(ProfileItem("EMAIL", uiState.user?.email ?: "Cargando..."))
            uiState.user?.phone?.let {
                if (it.isNotBlank()) {
                    add(ProfileItem("TELÉFONO", it))
                }
            }
            add(ProfileItem("UBICACIÓN", uiState.user?.city ?: "Cargando..."))
            add(ProfileItem("ROL", uiState.user?.role ?: "Cargando..."))
        }
    )

    val paymentMethods = listOf(
        "•••• •••• •••• 1234" to "Eliminar",
        "•••• •••• •••• 5678" to "Eliminar"
    )

    val shippingInfo = ProfileSection(
        title = "DIRECCIONES DE ENVÍO",
        items = listOf(
            ProfileItem("DIRECCIÓN PRINCIPAL", "Calle 45 #23-67, Laureles"),
            ProfileItem("DIRECCIÓN SECUNDARIA", "Agregar dirección adicional")
        )
    )

    val sizes = listOf("XS", "S", "M", "L", "XL", "XXL", "28", "30")

    // Determinar emoji según género
    val genderEmoji = when (uiState.user?.gender?.lowercase()) {
        "masculino" -> "👨"
        "femenino" -> "👩"
        "otro" -> "🧑"
        else -> "👤"
    }

    // Calcular edad aproximada (si hay fecha de nacimiento)
    val age = uiState.user?.birthDate?.let { birthDate ->
        try {
            val parts = birthDate.split("/", "-")
            if (parts.size == 3) {
                val year = parts[2].toIntOrNull()
                year?.let { 2025 - it }
            } else null
        } catch (e: Exception) {
            null
        }
    }

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

                    Text(
                        text = "Configuración",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = colors.onSurface
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onSavedClick) {
                            Icon(
                                imageVector = Icons.Default.Bookmarks,
                                contentDescription = "Guardados",
                                tint = colors.onSurface
                            )
                        }
                    }
                }
            }
        }
    ) { inner ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(inner),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Perfil del usuario
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = genderEmoji, fontSize = 40.sp)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val firstName = uiState.user?.fullName?.split(" ")?.firstOrNull() ?: "Usuario"
                            val displayText = if (age != null) "$firstName, $age" else firstName

                            Text(
                                text = displayText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )

                            Text(
                                text = uiState.user?.role ?: "Cargando...",
                                fontSize = 14.sp,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    // Información Personal
                    ProfileSectionCard(
                        section = personalInfo,
                        onItemClick = { /* TODO: abrir pantalla de edición */ }
                    )
                }

                item {
                    // Métodos de Pago
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "MÉTODOS DE PAGO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            paymentMethods.forEach { (cardNumber, action) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = cardNumber,
                                            fontSize = 14.sp,
                                            color = colors.onSurface
                                        )
                                    }

                                    TextButton(onClick = { /* TODO: eliminar tarjeta */ }) {
                                        Text(
                                            text = action,
                                            color = colors.primary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: agregar método de pago */ }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Agregar método de pago",
                                    fontSize = 14.sp,
                                    color = colors.primary
                                )
                            }
                        }
                    }
                }

                item {
                    // Direcciones de Envío
                    ProfileSectionCard(
                        section = shippingInfo,
                        onItemClick = { /* TODO: abrir detalle/edición de dirección */ }
                    )
                }

                item {
                    // Tallas Preferidas
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "TALLAS PREFERIDAS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                sizes.take(4).forEach { size ->
                                    SizeChip(
                                        size = size,
                                        isSelected = selectedSizes.contains(size),
                                        onToggle = {
                                            selectedSizes =
                                                if (selectedSizes.contains(size)) selectedSizes - size
                                                else selectedSizes + size
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                sizes.drop(4).forEach { size ->
                                    SizeChip(
                                        size = size,
                                        isSelected = selectedSizes.contains(size),
                                        onToggle = {
                                            selectedSizes =
                                                if (selectedSizes.contains(size)) selectedSizes - size
                                                else selectedSizes + size
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Configuración
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "CONFIGURACIÓN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "IDIOMA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: abrir selector de idioma */ }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Español",
                                    fontSize = 14.sp,
                                    color = colors.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    // Botón Guardar Cambios
                    Button(
                        onClick = { /* TODO: guardar cambios */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = "Guardar Cambios",
                            color = colors.onPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                item {
                    // Ayuda y Soporte
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "AYUDA Y SOPORTE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: abrir centro de ayuda */ }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Help,
                                    contentDescription = null,
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Centro de ayuda",
                                    fontSize = 14.sp,
                                    color = colors.onSurface
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: abrir FAQ */ }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QuestionAnswer,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "FAQ y tutoriales",
                                    fontSize = 14.sp,
                                    color = colors.onSurface
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: contactar soporte */ }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Support,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Contactar soporte",
                                    fontSize = 14.sp,
                                    color = colors.onSurface
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: enviar mensaje */ }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Enviar mensaje",
                                    fontSize = 14.sp,
                                    color = colors.onSurface
                                )
                            }
                        }
                    }
                }

                item {
                    // Botones de acción
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !uiState.isLoading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.primary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                width = 1.dp,
                                brush = SolidColor(colors.primary)
                            ),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = colors.primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Cerrar Sesión",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        TextButton(
                            onClick = onDeleteAccountClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colors.error
                            )
                        ) {
                            Text(
                                text = "Eliminar cuenta",
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }

            // Mostrar error si existe
            uiState.errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = colors.errorContainer,
                    contentColor = colors.onErrorContainer
                ) {
                    Text(text = error)
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    section: ProfileSection,
    onItemClick: (ProfileItem) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = section.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            section.items.forEachIndexed { index, item ->
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = item.isEditable) { onItemClick(item) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.value,
                                fontSize = 14.sp,
                                color = colors.onSurface
                            )
                        }

                        if (item.isEditable) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (index < section.items.size - 1) {
                        HorizontalDivider(
                            color = colors.outlineVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SizeChip(
    size: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    ElevatedCard(
        modifier = modifier
            .clickable { onToggle() }
            .height(40.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.primary else colors.surface
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = size,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) colors.onPrimary else colors.onSurface
            )
        }
    }
}

@Preview(showBackground = true, name = "Profile – Light")
@Composable
private fun ProfilePreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        ProfileScreen()
    }
}