package com.example.posapp.models

import android.icu.util.Calendar
import android.icu.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Locale

data class Orders(
    val totalPrice: Int,
    val deliveryDate: String,
    val userId: Int,
    val notes: String?= null,
    val orderDate: String = getCurrentVietnamTime()
) {
    companion object {
        fun getCurrentVietnamTime(): String {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(calendar.time)
        }
    }
}
