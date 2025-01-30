package com.example.posapp.models

data class OrderDetail(
    val id: Int,
    val orderId: Int,
    val productId: Int,
    val productPrice: Int,
    var quantity: Int,
    var subTotal: Int
)
