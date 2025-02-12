package com.example.posapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.util.Log
import com.example.posapp.models.Categories
import com.example.posapp.models.OrderDetail
import com.example.posapp.models.Orders
import com.example.posapp.models.Products
import com.example.posapp.models.Users
import java.text.SimpleDateFormat
import java.util.Locale

class Databases(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "BizPosApp.db"
        private const val DATABASE_VERSION =
            10  // change version number when changing table structure

        // Users Table
        const val USERS_TABLE = "users"
        const val USER_COLUMN_ID = "id"
        const val USER_COLUMN_EMAIL = "email"
        const val USER_COLUMN_PHONE = "phone"
        const val USER_COLUMN_PASS = "password"

        // Products Table
        const val PRODUCTS_TABLE = "products"
        const val PRODUCT_COLUMN_ID = "id"
        const val PRODUCT_COLUMN_NAME = "name"
        const val PRODUCT_COLUMN_PRICE = "price"
        const val PRODUCT_COLUMN_IMAGE = "image"
        const val PRODUCT_COLUMN_CATEGORY = "categoryId"

        // Category Table
        const val CATEGORIES_TABLE = "categories"
        const val CATEGORY_COLUMN_ID = "id"
        const val CATEGORY_COLUMN_NAME = "name"

        // Order Table
        const val ORDERS_TABLE = "orders"
        const val ORDER_ID = "id"
        const val ORDER_TOTAL_PRICE = "totalPrice"
        const val ORDER_DATE = "orderDate"
        const val DELIVERY_DATE = "deliveryDate"
        const val ORDER_USER = "userId"
        const val ORDER_NOTES = "notes"
        const val ORDER_CUSTOMER_NAME = "customer_name"
        const val ORDER_CUSTOMER_PHONE = "customer_phone"
        const val ORDER_CUSTOMER_ADDRESS = "customer_address"
        const val ORDER_STATUS = "status"

        // Order Details Table
        const val ORDER_DETAILS_TABLE = "order_details"
        const val ORDER_DETAILS_ID = "order_detail_id"
        const val COLUMN_ORDER_ID_FK = "order_id"
        const val ORDER_DETAILS_PRODUCT = "product_id"
        const val COLUMN_PRODUCT_PRICE = "product_price"
        const val ORDER_DETAILS_QUANTITY = "quantity"
        const val ORDER_DETAILS_TOTAL_PRICE = "total_price"

        var instance: Databases? = null

        @Synchronized
        fun getInstance(context: Context): Databases {
            return if (instance == null) {
                Databases(context.applicationContext).also {
                    instance = it
                }
            } else {
                instance!!
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create User Table
        val createUsersTable = """
                CREATE TABLE $USERS_TABLE (
                    $USER_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $USER_COLUMN_EMAIL TEXT,
                    $USER_COLUMN_PHONE TEXT,
                    $USER_COLUMN_PASS TEXT
                )
            """.trimIndent()

        // Create Category Table
        val createCategoriesTable = """
                CREATE TABLE $CATEGORIES_TABLE (
                    $CATEGORY_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $CATEGORY_COLUMN_NAME TEXT
                )
            """.trimIndent()

        // Create Product Table
        val createProductsTable = """
                CREATE TABLE $PRODUCTS_TABLE (
                    $PRODUCT_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $PRODUCT_COLUMN_NAME TEXT,
                    $PRODUCT_COLUMN_PRICE INTEGER,
                    $PRODUCT_COLUMN_IMAGE TEXT,
                    $PRODUCT_COLUMN_CATEGORY INTEGER
                )
            """.trimIndent()

        // Create Order Table
        val createOrderTable = """
                CREATE TABLE $ORDERS_TABLE (
                    $ORDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $ORDER_TOTAL_PRICE INTEGER,
                    $ORDER_DATE DATETIME DEFAULT CURRENT_TIMESTAMP,
                    $DELIVERY_DATE TEXT,
                    $ORDER_USER INTEGER,
                    $ORDER_NOTES TEXT,
                    $ORDER_CUSTOMER_NAME TEXT,
                    $ORDER_CUSTOMER_PHONE TEXT,
                    $ORDER_CUSTOMER_ADDRESS TEXT,
                    $ORDER_STATUS TEXT
                )
            """.trimIndent()

        // Create Order Details Table
        val createOrderDetailsTable = """
                CREATE TABLE $ORDER_DETAILS_TABLE (
                    $ORDER_DETAILS_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_ORDER_ID_FK INTEGER,
                    $ORDER_DETAILS_PRODUCT INTEGER,
                    $COLUMN_PRODUCT_PRICE INTEGER,
                    $ORDER_DETAILS_QUANTITY INTEGER,
                    $ORDER_DETAILS_TOTAL_PRICE INTEGER
                )
            """.trimIndent()

        db?.execSQL(createOrderDetailsTable)
        db?.execSQL(createOrderTable)
        db?.execSQL(createCategoriesTable)
        db?.execSQL(createProductsTable)
        db?.execSQL(createUsersTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $USERS_TABLE")
        db?.execSQL("DROP TABLE IF EXISTS $CATEGORIES_TABLE")
        db?.execSQL("DROP TABLE IF EXISTS $PRODUCTS_TABLE")
        db?.execSQL("DROP TABLE IF EXISTS $ORDERS_TABLE")
        db?.execSQL("DROP TABLE IF EXISTS $ORDER_DETAILS_TABLE")
        onCreate(db)
    }
}