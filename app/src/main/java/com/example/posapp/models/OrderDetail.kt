package com.example.posapp.models

data class OrderDetail(
    val orderId: Int,
    val productId: Int,
    val productPrice: Int,
    val quantity: Int,
    val subTotal: Int
)
