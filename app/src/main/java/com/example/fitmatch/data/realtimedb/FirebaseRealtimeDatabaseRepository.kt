package com.example.fitmatch.data.realtimedb

import com.example.fitmatch.model.order.CartItem
import com.example.fitmatch.model.product.Product
import com.example.fitmatch.model.social.Chat
import com.example.fitmatch.model.social.Message
import com.example.fitmatch.model.social.Notification
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.fitmatch.data.realtimedb.RealtimeDatabaseRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRealtimeDatabaseRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : RealtimeDatabaseRepository {

    private val productsRef get() = database.getReference("products")
    private val cartsRef get() = database.getReference("carts")
    private val chatsRef get() = database.getReference("chats")
    private val userChatsRef get() = database.getReference("userChats")
    private val notificationsRef get() = database.getReference("notifications")

    override suspend fun saveProduct(product: Product): String {
        val vendorNode = productsRef.child(product.vendorId)
        val productId = product.id.ifBlank { vendorNode.push().key ?: UUID.randomUUID().toString() }
        val now = System.currentTimeMillis()
        val productToSave = product.copy(
            id = productId,
            createdAt = if (product.createdAt == 0L) now else product.createdAt,
            updatedAt = now
        )

        vendorNode.child(productId).setValue(productToSave).await()
        return productId
    }

    override fun observeVendorProducts(vendorId: String): Flow<List<Product>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val ref = productsRef.child(vendorId)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
    override fun observeAllProducts(): Flow<List<Product>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children
                    .flatMap { vendorSnapshot ->
                        vendorSnapshot.children.mapNotNull { it.getValue(Product::class.java) }
                    }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        productsRef.addValueEventListener(listener)
        awaitClose { productsRef.removeEventListener(listener) }
    }
    override suspend fun updateProductStock(vendorId: String, productId: String, newStock: Int) {
        productsRef.child(vendorId).child(productId).child("stock").setValue(newStock).await()
    }

    override suspend fun addOrUpdateCartItem(userId: String, item: CartItem) {
        val cartId =
            item.id.ifBlank { cartsRef.child(userId).push().key ?: UUID.randomUUID().toString() }
        val normalized =
            item.copy(id = cartId, userId = userId, addedAt = System.currentTimeMillis())
        cartsRef.child(userId).child(cartId).setValue(normalized).await()
    }

    override suspend fun updateCartItemQuantity(userId: String, itemId: String, quantity: Int) {
        cartsRef.child(userId).child(itemId).child("quantity").setValue(quantity).await()
    }

    override suspend fun removeCartItem(userId: String, itemId: String) {
        cartsRef.child(userId).child(itemId).removeValue().await()
    }

    override fun observeCart(userId: String): Flow<List<CartItem>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val ref = cartsRef.child(userId)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun createChat(chat: Chat): String {
        val chatId = chat.id.ifBlank { chatsRef.push().key ?: UUID.randomUUID().toString() }
        val now = System.currentTimeMillis()
        val chatToSave = chat.copy(id = chatId, lastMessageAt = now)
        chatsRef.child(chatId).setValue(chatToSave).await()
        chat.participantIds.forEach { userId ->
            userChatsRef.child(userId).child(chatId).setValue(true)
        }
        return chatId
    }

    override fun observeUserChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val chatListeners = mutableMapOf<String, ValueEventListener>()
        val chatsById = mutableMapOf<String, Chat>()

        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatIds = snapshot.children.mapNotNull { it.key }.toSet()

                // Remove listeners for chats that no longer belong to the user
                val removed = chatListeners.keys - chatIds
                removed.forEach { chatId ->
                    chatsRef.child(chatId).removeEventListener(chatListeners[chatId]!!)
                    chatListeners.remove(chatId)
                    chatsById.remove(chatId)
                }

                // Attach listeners for new chats
                val newIds = chatIds - chatListeners.keys
                newIds.forEach { chatId ->
                    val chatListener = object : ValueEventListener {
                        override fun onDataChange(chatSnapshot: DataSnapshot) {
                            chatSnapshot.getValue(Chat::class.java)?.let {
                                chatsById[chatId] = it
                                trySend(chatsById.values.sortedByDescending { chat -> chat.lastMessageAt })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            close(error.toException())
                        }
                    }
                    chatListeners[chatId] = chatListener
                    chatsRef.child(chatId).addValueEventListener(chatListener)
                }

                if (chatIds.isEmpty()) {
                    chatsById.clear()
                    trySend(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val ref = userChatsRef.child(userId)
        ref.addValueEventListener(userListener)
        awaitClose {
            ref.removeEventListener(userListener)
            chatListeners.forEach { (chatId, listener) ->
                chatsRef.child(chatId).removeEventListener(listener)
            }
        }
    }

    override fun observeChatMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val ref = chatsRef.child(chatId).child("messages")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                trySend(messages.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun sendMessage(chatId: String, message: Message) {
        val messageId = message.id.ifBlank { chatsRef.push().key ?: UUID.randomUUID().toString() }
        val normalized = message.copy(
            id = messageId,
            timestamp = if (message.timestamp == 0L) System.currentTimeMillis() else message.timestamp
        )
        chatsRef.child(chatId).child("messages").child(messageId).setValue(normalized).await()
        chatsRef.child(chatId).child("lastMessage").setValue(message.text).await()
        chatsRef.child(chatId).child("lastMessageAt").setValue(normalized.timestamp).await()
    }

    override suspend fun pushNotification(notification: Notification) {
        val id =
            notification.id.ifBlank { notificationsRef.push().key ?: UUID.randomUUID().toString() }
        val normalized = notification.copy(
            id = id,
            createdAt = if (notification.createdAt == 0L) System.currentTimeMillis() else notification.createdAt
        )
        notificationsRef.child(notification.userId).child(id).setValue(normalized).await()
    }
}
