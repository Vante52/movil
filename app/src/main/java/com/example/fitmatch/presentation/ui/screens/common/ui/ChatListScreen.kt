package com.example.fitmatch.presentation.ui.screens.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitmatch.presentation.viewmodel.user.ChatListViewModel
import com.example.fitmatch.presentation.viewmodel.user.ChatRowState
import com.example.fitmatch.presentation.viewmodel.user.ChatUserRow

@Composable
fun ChatListScreen(
    vm: ChatListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onOpenChat: (chatId: String, isTito: Boolean) -> Unit = { _, _ -> },
    onNewChat: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var showUserResults by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
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
                            contentDescription = "Atrás",
                            tint = colors.onSurface
                        )
                    }

                    Text(
                        text = "Chats",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = colors.onSurface
                        ),
                        modifier = Modifier.align(Alignment.Center),
                    )

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onMoreClick) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Más",
                                tint = colors.onSurface
                            )
                        }
                    }
                }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    showUserResults = it.isNotBlank()
                    vm.searchUsers(it)
                },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("Buscar chat o usuario…") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Spacer(Modifier.height(10.dp))

            FilterRow(
                options = listOf("Todos", "No leídos", "Tiendas"),
                selected = selectedFilter,
                onSelect = { selectedFilter = it }
            )

            Spacer(Modifier.height(8.dp))

            val filteredChats = uiState.chats
                .filter { it.title.contains(query, ignoreCase = true) || it.subtitle.contains(query, ignoreCase = true) }
                .filter { chat ->
                    when (selectedFilter) {
                        "No leídos" -> chat.unread > 0
                        else -> true
                    }
                }
            val titoChat = filteredChats.firstOrNull { it.isTito }
            val regularChats = filteredChats.filterNot { it.isTito }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.error!!, color = colors.error)
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        titoChat?.let { row ->
                            item {
                                TitoPinnedCard(
                                    row = row,
                                    onClick = { onOpenChat(row.id, true) }
                                )
                            }
                        }
                        items(regularChats, key = { it.id }) { row ->
                            ChatListCard(
                                row = row,
                                onClick = { onOpenChat(row.id, row.isTito) }
                            )
                        }
                        if (showUserResults) {
                            item {
                                Text(
                                    text = "Usuarios",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            when {
                                uiState.isSearchingUsers -> {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }

                                uiState.userSearchError != null -> {
                                    item {
                                        Text(
                                            text = uiState.userSearchError!!,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }

                                uiState.userResults.isEmpty() -> {
                                    item {
                                        Text(
                                            text = "No se encontraron usuarios",
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }

                                else -> {
                                    items(uiState.userResults, key = { it.id }) { user ->
                                        UserResultCard(
                                            user = user,
                                            isLoading = uiState.isStartingChat,
                                            onClick = {
                                                vm.openChatWithUser(user.id) { chatId ->
                                                    onOpenChat(chatId, false)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        if (filteredChats.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No hay conversaciones todavía")
                                }
                            }
                        }
                        item {
                            OutlinedButton(
                                onClick = {
                                    showUserResults = true
                                    vm.loadAllUsers()
                                    onNewChat()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Nuevo chat", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { opt ->
            FilterChip(
                selected = selected == opt,
                onClick = { onSelect(opt) },
                label = { Text(opt) }
            )
        }
    }
}

@Composable
private fun TitoPinnedCard(
    row: ChatRowState,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    androidx.compose.material3.ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        ListItem(
            leadingContent = {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                    )
                }
            },
            headlineContent = {
                Text(row.title, fontWeight = FontWeight.SemiBold)
            },
            supportingContent = { Text(row.subtitle) },
            trailingContent = {
                AssistChip(
                    onClick = onClick,
                    label = { Text("Fijado") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.PushPin,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        )
    }
}

@Composable
private fun ChatListCard(
    row: ChatRowState,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        ListItem(
            leadingContent = {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceVariant)
                )
            },
            headlineContent = {
                Text(row.title, fontWeight = FontWeight.SemiBold)
            },
            supportingContent = { Text(row.subtitle) },
            trailingContent = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    row.time.ifBlank { null }?.let { Text(it, fontSize = 12.sp, color = colors.onSurfaceVariant) }
                    if (row.unread > 0) {
                        BadgedBox(badge = { Badge { Text("${row.unread}") } }) {
                            Spacer(Modifier.size(1.dp))
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun UserResultCard(
    user: ChatUserRow,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onClick)
    ) {
        ListItem(
            leadingContent = {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceVariant)
                )
            },
            headlineContent = {
                Text(user.name.ifBlank { "Usuario" }, fontWeight = FontWeight.SemiBold)
            },
            supportingContent = {
                Text(user.email)
            },
            trailingContent = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }
        )
    }
}
