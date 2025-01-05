package com.example.posapp.models

import java.io.Serializable

data class Products(
    val id: Int,
    val name: String,
    val price: Int,
    val imageResId: String,
    val category: Int
) : Serializable    // cho phép đối tượng Products được truyền qua Intent giữa các Activity