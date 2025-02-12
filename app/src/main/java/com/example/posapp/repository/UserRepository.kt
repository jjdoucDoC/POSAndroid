package com.example.posapp.repository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import com.example.posapp.Databases
import com.example.posapp.Databases.Companion.USERS_TABLE
import com.example.posapp.Databases.Companion.USER_COLUMN_EMAIL
import com.example.posapp.Databases.Companion.USER_COLUMN_ID
import com.example.posapp.Databases.Companion.USER_COLUMN_PASS
import com.example.posapp.Databases.Companion.USER_COLUMN_PHONE
import com.example.posapp.models.Users

class UserRepository(private val db : Databases) {
    companion object {
        var instance: UserRepository? = null
        @Synchronized
        fun getInstance(context: Context): UserRepository {
            return if (instance == null) {
                UserRepository(Databases.getInstance(context)).also {
                    instance = it
                }
            } else {
                instance!!
            }
        }
    }

    fun insertUser(users: Users): Boolean {
        val db = db.writableDatabase
        val values = ContentValues().apply {
            put(USER_COLUMN_EMAIL, users.email)
            put(USER_COLUMN_PHONE, users.phone)
            put(USER_COLUMN_PASS, users.passWord)
        }
        val result = db.insert(USERS_TABLE, null, values)
        db.close()
        return result != -1L
    }

    /**
     * Check Valid Login
     */
    @SuppressLint("Range")
    fun isValidUser(emailOrPhone: String, password: String): Int {
        val db = db.readableDatabase
        val query = """
                SELECT * FROM $USERS_TABLE
                WHERE ($USER_COLUMN_EMAIL = ? OR $USER_COLUMN_PHONE = ?) 
                AND $USER_COLUMN_PASS = ?
            """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(emailOrPhone, emailOrPhone, password))

        // If the user exists, return the userId, otherwise return -1
        val userId = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndex(USER_COLUMN_ID))
        } else {
            -1
        }

        cursor.close()
        db.close()
        return userId
    }

    // Check If User Exists (by Email or Phone)
    fun checkUserExists(email: String, phone: String): Boolean {
        val db = db.readableDatabase
        val query = """
                SELECT * FROM $USERS_TABLE
                WHERE $USER_COLUMN_EMAIL = ? OR $USER_COLUMN_PHONE = ?
            """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(email, phone))

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }
}