package com.example.posapp.models

data class Orders(
    val id: Int,
    val totalPrice: Int,
    val date: String,
    val userId: Int,
    val notes: String ?= null   // Ghi chú, có thể null
)
