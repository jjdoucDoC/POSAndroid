package com.example.posapp.models

data class Products(
    val id: Int,
    val name: String,
    val price: Int,
    val imageResId: String,
    val category: Int
) {
}