package com.example.fitmatch.presentation.viewmodel.user

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.fitmatch.data.realtimedb.FirebaseRealtimeDatabaseRepository
import com.example.fitmatch.data.realtimedb.RealtimeDatabaseRepository
import com.example.fitmatch.data.user.FirebaseUserRepository
import com.example.fitmatch.data.user.UserRepository
import com.example.fitmatch.model.social.Message
import com.example.fitmatch.model.social.Chat
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    val isTito: Boolean,
    val otherUserId: String? = null
)

data class ChatUserRow(
    val id: String,
    val name: String,
    val email: String
)

data class ChatListUiState(
    val chats: List<ChatRowState> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val userResults: List<ChatUserRow> = emptyList(),
    val isSearchingUsers: Boolean = false,
    val userSearchError: String? = null,
    val isStartingChat: Boolean = false
)

class ChatListViewModel(
    private val realtimeRepo: RealtimeDatabaseRepository = FirebaseRealtimeDatabaseRepository(),
    private val userRepo: UserRepository = FirebaseUserRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private val userNameCache = mutableMapOf<String, String>()

    init {
        observeChats()
    }

    private fun observeChats() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = ChatListUiState(
                isLoading = false,
                error = "Debes iniciar sesión para ver tus chats"
            )
            return
        }

        viewModelScope.launch(dispatcher) {
            realtimeRepo.observeUserChats(userId)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Error al cargar tus chats"
                        )
                    }
                        }
                        .collect { chats ->
                            val rows = chats.map { chat ->
                                val otherParticipant = chat.participantIds.firstOrNull { id -> id != userId }
                                val resolvedName = try {
                                    loadUserName(otherParticipant)
                                } catch (_: Exception) {
                                    otherParticipant?.takeIf { it.isNotBlank() } ?: "Chat"
                                }

                                ChatRowState(
                                    id = chat.id,
                                    title = resolvedName,
                                    subtitle = chat.lastMessage.ifBlank { "Empieza la conversación" },
                                    time = formatTimestamp(chat.lastMessageAt),
                                    unread = chat.unreadCount[userId] ?: 0,
                                    isTito = chat.isTito,
                                    otherUserId = otherParticipant
                                )
                            }

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = null,
                                    chats = rows
                                )
                            }
                        }
        }
    }

    fun openChatWithUser(userId: String, onChatReady: (String) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _uiState.update { it.copy(error = "Debes iniciar sesión para iniciar un chat") }
            return
        }

        viewModelScope.launch(dispatcher) {
            try {
                _uiState.update { it.copy(isStartingChat = true, error = null) }
                val existingChat = _uiState.value.chats.firstOrNull { it.otherUserId == userId }
                val chatId = existingChat?.id ?: realtimeRepo.createChat(
                    Chat(
                        participantIds = listOf(currentUserId, userId)
                    )
                )
                onChatReady(chatId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "No se pudo iniciar el chat") }
            } finally {
                _uiState.update { it.copy(isStartingChat = false) }
            }
        }
    }

    fun searchUsers(query: String) {
        val currentUserId = auth.currentUser?.uid

        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    userResults = emptyList(),
                    isSearchingUsers = false,
                    userSearchError = null
                )
            }
            return
        }

        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isSearchingUsers = true, userSearchError = null) }
            try {
                val users = userRepo.searchUsers(query, excludeUserId = currentUserId)
                _uiState.update {
                    it.copy(
                        isSearchingUsers = false,
                        userResults = users.map { user ->
                            ChatUserRow(
                                id = user.id,
                                name = user.fullName.ifBlank { user.email },
                                email = user.email
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearchingUsers = false, userSearchError = e.message ?: "Error al buscar usuarios") }
            }
        }
    }

    fun loadAllUsers() {
        val currentUserId = auth.currentUser?.uid
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isSearchingUsers = true, userSearchError = null) }
            try {
                val users = userRepo.getAllUsers(excludeUserId = currentUserId)
                _uiState.update {
                    it.copy(
                        isSearchingUsers = false,
                        userResults = users.map { user ->
                            ChatUserRow(
                                id = user.id,
                                name = user.fullName.ifBlank { user.email },
                                email = user.email
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearchingUsers = false, userSearchError = e.message ?: "Error al cargar usuarios") }
            }
        }
    }
    private suspend fun loadUserName(userId: String?): String {
        if (userId.isNullOrBlank()) return "Chat"

        userNameCache[userId]?.let { cached ->
            return cached
        }

        val user = userRepo.getUserById(userId)
        val resolved = user?.fullName?.ifBlank { user.email } ?: "Chat"
        userNameCache[userId] = resolved
        return resolved
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
    companion object {
        fun provideFactory(
            chatId: String,
            realtimeRepo: RealtimeDatabaseRepository = FirebaseRealtimeDatabaseRepository(),
            auth: FirebaseAuth = FirebaseAuth.getInstance(),
            dispatcher: CoroutineDispatcher = Dispatchers.IO
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val savedStateHandle = extras.createSavedStateHandle()
                savedStateHandle["chatId"] = chatId
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(
                    savedStateHandle = savedStateHandle,
                    realtimeRepo = realtimeRepo,
                    auth = auth,
                    dispatcher = dispatcher
                ) as T
            }
        }
    }
}
