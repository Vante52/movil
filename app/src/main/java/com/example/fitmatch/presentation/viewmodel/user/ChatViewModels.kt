package com.example.fitmatch.presentation.viewmodel.user

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.data.realtimedb.FirebaseRealtimeDatabaseRepository
import com.example.fitmatch.data.realtimedb.RealtimeDatabaseRepository
import com.example.fitmatch.model.social.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private fun formatTimestamp(value: Long): String {
    if (value == 0L) return ""
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(value))
}

// --------- Listado de chats ---------
data class ChatRowState(
    val id: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val unread: Int,
    val isTito: Boolean
)

data class ChatListUiState(
    val chats: List<ChatRowState> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ChatListViewModel(
    private val realtimeRepo: RealtimeDatabaseRepository = FirebaseRealtimeDatabaseRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        observeChats()
    }

    private fun observeChats() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = ChatListUiState(isLoading = false, error = "Debes iniciar sesión para ver tus chats")
            return
        }

        viewModelScope.launch(dispatcher) {
            realtimeRepo.observeUserChats(userId).collect { chats ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        chats = chats.map { chat ->
                            val otherParticipant = chat.participantIds.firstOrNull { id -> id != userId }
                            ChatRowState(
                                id = chat.id,
                                title = otherParticipant?.takeIf { it.isNotBlank() } ?: "Chat",
                                subtitle = chat.lastMessage.ifBlank { "Empieza la conversación" },
                                time = formatTimestamp(chat.lastMessageAt),
                                unread = chat.unreadCount[userId] ?: 0,
                                isTito = chat.isTito
                            )
                        }
                    )
                }
            }
        }
    }
}

// --------- Chat individual ---------
data class ChatUiState(
    val chatId: String = "",
    val messages: List<Message> = emptyList(),
    val messageText: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null
)

class ChatViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val realtimeRepo: RealtimeDatabaseRepository = FirebaseRealtimeDatabaseRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatUiState(chatId = savedStateHandle.get<String>("chatId") ?: "")
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        val chatId = _uiState.value.chatId
        _uiState.update { it.copy(currentUserId = auth.currentUser?.uid) }
        if (chatId.isNotBlank()) {
            observeMessages(chatId)
        } else {
            _uiState.update { it.copy(error = "No se pudo abrir el chat") }
        }
    }

    private fun observeMessages(chatId: String) {
        viewModelScope.launch(dispatcher) {
            realtimeRepo.observeChatMessages(chatId).collect { messages ->
                _uiState.update { it.copy(messages = messages.sortedBy { msg -> msg.timestamp }) }
            }
        }
    }

    fun onMessageChange(text: String) {
        _uiState.update { it.copy(messageText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.messageText.trim()
        val chatId = _uiState.value.chatId
        val senderId = auth.currentUser?.uid
        if (text.isBlank() || chatId.isBlank()) return
        if (senderId == null) {
            _uiState.update { it.copy(error = "Debes iniciar sesión para enviar mensajes") }
            return
        }

        viewModelScope.launch(dispatcher) {
            try {
                _uiState.update { it.copy(isSending = true, error = null) }
                realtimeRepo.sendMessage(
                    chatId,
                    Message(
                        chatId = chatId,
                        senderId = senderId,
                        text = text,
                        timestamp = System.currentTimeMillis()
                    )
                )
                _uiState.update { it.copy(messageText = "") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Error al enviar el mensaje") }
            } finally {
                _uiState.update { it.copy(isSending = false) }
            }
        }
    }
}
