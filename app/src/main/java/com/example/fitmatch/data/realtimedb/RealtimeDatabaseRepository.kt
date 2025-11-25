package com.example.fitmatch.data.realtimedb

import com.example.fitmatch.model.order.CartItem
import com.example.fitmatch.model.product.Product
import com.example.fitmatch.model.social.Chat
import com.example.fitmatch.model.social.Message
import com.example.fitmatch.model.social.Notification
import kotlinx.coroutines.flow.Flow

interface RealtimeDatabaseRepository {
    suspend fun saveProduct(product: Product): String
    fun observeVendorProducts(vendorId: String): Flow<List<Product>>
    fun observeAllProducts(): Flow<List<Product>>
    suspend fun updateProductStock(vendorId: String, productId: String, newStock: Int)

    suspend fun addOrUpdateCartItem(userId: String, item: CartItem)
    suspend fun updateCartItemQuantity(userId: String, itemId: String, quantity: Int)
    suspend fun removeCartItem(userId: String, itemId: String)
    fun observeCart(userId: String): Flow<List<CartItem>>

    suspend fun createChat(chat: Chat): String
    fun observeUserChats(userId: String): Flow<List<Chat>>
    fun observeChatMessages(chatId: String): Flow<List<Message>>
    suspend fun sendMessage(chatId: String, message: Message)

    suspend fun pushNotification(notification: Notification)
}