package com.example.posapp.models

data class OrderDetails(
    val id: Int,
    val orderId: Int,
    val productId: Int,
    val productPrice: Int,
    val quantity: Int,
    val totalPrice: Int
)
