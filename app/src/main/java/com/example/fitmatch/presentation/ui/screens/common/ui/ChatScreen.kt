package com.example.fitmatch.presentation.ui.screens.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.FitMatchTheme
import com.example.fitmatch.model.social.Message
import com.example.fitmatch.presentation.viewmodel.user.ChatUiState
import com.example.fitmatch.presentation.viewmodel.user.ChatViewModel

@Composable
fun ChatScreen(
    chatId: String,
    vm: ChatViewModel = viewModel(factory = ChatViewModel.provideFactory(chatId)),
    contactName: String = "",
    contactSubtitle: String = "En línea",
    onBackClick: () -> Unit = {},
    onCallClick: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val headerTitle = contactName.ifBlank { "Chat ${chatId.takeLast(6)}" }

    ChatContent(
        state = state,
        headerTitle = headerTitle,
        contactSubtitle = contactSubtitle,
        onBackClick = onBackClick,
        onCallClick = onCallClick,
        onMoreClick = onMoreClick,
        onMessageChange = vm::onMessageChange,
        onSend = vm::sendMessage
    )
}

@Composable
private fun ChatContent(
    state: ChatUiState,
    headerTitle: String,
    contactSubtitle: String,
    onBackClick: () -> Unit,
    onCallClick: () -> Unit,
    onMoreClick: () -> Unit,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
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

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = headerTitle,
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

                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCallClick) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = "Llamar",
                            tint = colors.onSurface
                        )
                    }
                    IconButton(onClick = onMoreClick) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Más opciones",
                            tint = colors.onSurface
                        )
                    }
                }
            }
        }

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

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(state.messages) { message ->
                MessageBubble(
                    message = message,
                    isFromUser = message.senderId == state.currentUserId
                )
            }
        }

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
                    value = state.messageText,
                    onValueChange = onMessageChange,
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
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Mensaje de voz",
                        tint = colors.primary
                    )
                }

                IconButton(onClick = { /* emoji */ }) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEmotions,
                        contentDescription = "Emojis",
                        tint = colors.primary
                    )
                }

                IconButton(onClick = { /* imagen */ }) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = "Enviar imagen",
                        tint = colors.primary
                    )
                }

                IconButton(onClick = { /* ubicación */ }) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Enviar ubicación",
                        tint = colors.primary
                    )
                }

                IconButton(
                    enabled = state.messageText.isNotBlank() && !state.isSending,
                    onClick = onSend
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Enviar",
                        tint = if (state.messageText.isNotBlank()) colors.primary else colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isFromUser: Boolean
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    tint = colors.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

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

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
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

@Preview(showBackground = true, name = "Chat – Light (Brand)")
@Composable
private fun ChatPreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        ChatContent(
            state = ChatUiState(
                chatId = "preview",
                messages = listOf(
                    Message(text = "Hola, ¿cómo estás?", senderId = "other"),
                    Message(text = "Todo bien, gracias", senderId = "me")
                ),
                currentUserId = "me",
                messageText = "Mensaje..."
            ),
            headerTitle = "Helena Hills",
            contactSubtitle = "En línea",
            onBackClick = {},
            onCallClick = {},
            onMoreClick = {},
            onMessageChange = {},
            onSend = {}
        )
    }
}

@Preview(showBackground = true, name = "Chat – Dark (Brand)")
@Composable
private fun ChatPreviewDark() {
    FitMatchTheme(darkTheme = true, dynamicColor = false) {
        ChatPreviewLight()
    }
}
