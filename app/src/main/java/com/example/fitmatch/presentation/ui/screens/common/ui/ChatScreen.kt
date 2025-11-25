package com.example.fitmatch.presentation.ui.screens.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.FitMatchTheme

// ----------------------------
// Modelo
// ----------------------------
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String = ""
)

// ----------------------------
// Pantalla principal (sin @Preview aquí)
// ----------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contactName: String = "Helena Hills",
    contactSubtitle: String = "En línea",
    onBackClick: () -> Unit = {},
    onCallClick: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    var messageText by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    "Jean Azul Levis Talla S\n Puedes chatear con el vendedor para contactar tu compra.",
                    false
                ),
                ChatMessage("Ah, sí?", false),
                ChatMessage("Qué chulo", false),
                ChatMessage("Cómo funciona?", false),
                ChatMessage(
                    "Solo tienes que editar cualquier texto para escribir la conversación que quieras mostrar y borrar las burbujas que no quieras utilizar",
                    true
                ),
                ChatMessage("Mmm", false),
                ChatMessage("Creo que lo entiendo", false),
                ChatMessage("De todas formas míraré el Centro de ayuda si tengo más preguntas", false)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // ===================== Header UNIFICADO =====================
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
                // Back (izquierda)
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

                // Título centrado (nombre + subtítulo)
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = contactName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = colors.onSurface
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = contactSubtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                }

                // Acciones (derecha): llamar + más
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCallClick) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Llamar",
                            tint = colors.onSurface
                        )
                    }
                    IconButton(onClick = onMoreClick) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = colors.onSurface
                        )
                    }
                }
            }
        }
        // ============================================================

        // Mensaje/banner superior (informativo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.secondaryContainer)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Esta es la pantalla principal de chat",
                fontSize = 12.sp,
                color = colors.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }

        // Lista de mensajes
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    isFromUser = message.isFromUser
                )
            }
        }

        // Campo de entrada de mensaje
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Mensaje...", color = colors.onSurfaceVariant) },
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
                    shape = RoundedCornerShape(25.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = { /* mic */ }) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mensaje de voz",
                        tint = colors.primary
                    )
                }

                IconButton(onClick = { /* emoji */ }) {
                    Icon(
                        imageVector = Icons.Default.EmojiEmotions,
                        contentDescription = "Emojis",
                        tint = colors.primary
                    )
                }

                IconButton(onClick = { /* imagen */ }) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Enviar imagen",
                        tint = colors.primary
                    )
                }

                IconButton(onClick = { /* ubicación */ }) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Enviar ubicación",
                        tint = colors.primary
                    )
                }
            }
        }
    }
}

// ----------------------------
// Burbuja de mensaje
// ----------------------------
@Composable
private fun MessageBubble(
    message: ChatMessage,
    isFromUser: Boolean
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromUser) {
            // Avatar del contacto
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    tint = colors.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        // Burbuja del mensaje
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromUser) colors.primary else colors.surface
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromUser) 16.dp else 4.dp,
                bottomEnd = if (isFromUser) 4.dp else 16.dp
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (isFromUser) colors.onPrimary else colors.onSurface,
                fontSize = 14.sp
            )
        }

        if (isFromUser) {
            Spacer(modifier = Modifier.width(8.dp))

            // Avatar del usuario
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    tint = colors.primary
                )
            }
        }
    }
}


// Previews con el tema
@Preview(showBackground = true, name = "Chat – Light (Brand)")
@Composable
private fun ChatPreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        ChatScreen()
    }
}

@Preview(showBackground = true, name = "Chat – Dark (Brand)")
@Composable
private fun ChatPreviewDark() {
    FitMatchTheme(darkTheme = true, dynamicColor = false) {
        ChatScreen()
    }
}
