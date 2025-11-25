package com.example.fitmatch.model.order

import com.google.firebase.Timestamp

data class Order(
    val id: String,
    val orderNumber: String,           // "#MIX-24816"
    val buyerId: String,
    val sellerId: String,
    val items: List<OrderItem>,
    val subtotal: Int,
    val shippingCost: Int,
    val discount: Int,
    val total: Int,
    val status: String,                // "PENDIENTE" | "PREPARANDO" | "ENVIADO" | "ENTREGADO"
    val shippingAddress: Address,
    val paymentMethod: PaymentMethod,
    val createdAt: Timestamp,
    val updatedAt: Timestamp
)
