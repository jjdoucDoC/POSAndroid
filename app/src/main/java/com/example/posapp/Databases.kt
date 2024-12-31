package com.example.posapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.posapp.models.Categories
import com.example.posapp.models.Orders
import com.example.posapp.models.Products
import com.example.posapp.models.Users

class Databases(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "BizPosApp.db"
        private const val DATABASE_VERSION = 6  // change version number when changing table structure

        // Users Table
        private const val USERS_TABLE = "users"
        private const val USER_COLUMN_ID = "id"
        private const val USER_COLUMN_EMAIL = "email"
        private const val USER_COLUMN_PHONE = "phone"
        private const val USER_COLUMN_PASS = "password"

        // Products Table
        private const val PRODUCTS_TABLE = "products"
        private const val PRODUCT_COLUMN_ID = "id"
        private const val PRODUCT_COLUMN_NAME = "name"
        private const val PRODUCT_COLUMN_PRICE = "price"
        private const val PRODUCT_COLUMN_IMAGE = "image"
        private const val PRODUCT_COLUMN_CATEGORY = "categoryId"

        // Category Table
        private const val CATEGORIES_TABLE = "categories"
        private const val CATEGORY_COLUMN_ID = "id"
        private const val CATEGORY_COLUMN_NAME = "name"

        // Order Table
        private const val ORDERS_TABLE = "orders"
        private const val ORDER_ID = "id"
        private const val ORDER_TOTAL_PRICE = "totalPrice"
        private const val ORDER_DATE = "date"
        private const val ORDER_USER = "userId"
        private const val ORDER_NOTES = "notes"

        // Order Details Table
        private const val ODER_DETAILS_TABLE = "details"
        private const val ODER_DETAILS_ID = "id"
        private const val ODER_DETAILS_ORDER = "orderId"
        private const val ODER_DETAILS_PRICE = "productPrice"
        private const val ODER_DETAILS_QUANTITY = "quantity"
        private const val ODER_DETAILS_TOTAL_PRICE = "totalPrice"
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
                $ORDER_USER INTEGER,
                $ORDER_NOTES TEXT
            )
        """.trimIndent()

        // Create Order Details Table
        val createOrderDetailsTable = """
            CREATE TABLE $ODER_DETAILS_TABLE (
                $ODER_DETAILS_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $ODER_DETAILS_ORDER INTEGER,
                $ODER_DETAILS_PRICE INTEGER,
                $ODER_DETAILS_QUANTITY INTEGER,
                $ODER_DETAILS_TOTAL_PRICE INTEGER
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
        db?.execSQL("DROP TABLE IF EXISTS $ODER_DETAILS_TABLE")
        onCreate(db)
    }

    /*---------------User Method--------------------------*/
    // Add User
    fun insertUser(users: Users): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(USER_COLUMN_EMAIL, users.email)
            put(USER_COLUMN_PHONE, users.phone)
            put(USER_COLUMN_PASS, users.passWord)
        }
        val result = db.insert(USERS_TABLE, null, values)
        db.close()
        return result != -1L
    }

    // Check Valid Login
    fun isValidUser(emailOrPhone: String, password: String): Boolean {
        val db = readableDatabase
        val query = """
            SELECT * FROM $USERS_TABLE
            WHERE ($USER_COLUMN_EMAIL = ? OR $USER_COLUMN_PHONE = ?) 
            AND $USER_COLUMN_PASS = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(emailOrPhone, emailOrPhone, password))

        val isValid = cursor.count > 0
        cursor.close()
        db.close()
        return isValid
    }

    // Check If User Exists (by Email or Phone)
    fun checkUserExists(email: String, phone: String): Boolean {
        val db = readableDatabase
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

    /*---------------Category Method--------------------------*/
    // Add Category
    fun insertCategory (category: Categories) : Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(CATEGORY_COLUMN_NAME, category.name)
        }
        val result = db.insert(CATEGORIES_TABLE, null, values)
        db.close()

        return result != -1L
    }

    // Get All Category
    fun getCategory() : List<Categories> {
        val categoryList = mutableListOf<Categories>()
        val db = readableDatabase
        val query = "SELECT * FROM $CATEGORIES_TABLE"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_COLUMN_NAME))

            val cat = Categories(id, name)
            categoryList.add(cat)
        }

        cursor.close()
        db.close()

        return categoryList
    }

    // Get Category ID by Name
    fun getCategoryIdByName(categoryName: String): Int? {
        val db = readableDatabase
        val query = "SELECT $CATEGORY_COLUMN_ID FROM $CATEGORIES_TABLE WHERE $CATEGORY_COLUMN_NAME = ?"
        val cursor = db.rawQuery(query, arrayOf(categoryName))

        var categoryId: Int? = null
        if (cursor.moveToFirst()) {
            categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_COLUMN_ID))
        }

        cursor.close()
        db.close()
        return categoryId
    }

    // Get Details Category by ID
    fun getCategoryByID (categoryId: Int) : Categories {
        val db = readableDatabase
        val query = "SELECT * FROM $CATEGORIES_TABLE WHERE $CATEGORY_COLUMN_ID = $categoryId"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()

        val id = cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_COLUMN_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_COLUMN_NAME))

        cursor.close()
        db.close()
        return Categories(id, name)
    }

    // Update Category
    fun updateCategory (category: Categories) : Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(CATEGORY_COLUMN_NAME, category.name)
        }
        val whereClause = "$CATEGORY_COLUMN_ID = ?"
        val whereArgs = arrayOf(category.id.toString())
        val result = db.update(CATEGORIES_TABLE, values, whereClause, whereArgs)
        db.close()

        return result > 0
    }

    // Delete Category
    fun deleteCategory (categoryId: Int) : Boolean {
        val db = writableDatabase

        val deleteProductQuerry = "DELETE FROM $PRODUCTS_TABLE WHERE $PRODUCT_COLUMN_CATEGORY = ?"
        db.execSQL(deleteProductQuerry, arrayOf(categoryId.toString()))

        val whereClause = "$CATEGORY_COLUMN_ID = ?"
        val whereArgs = arrayOf(categoryId.toString())
        val result = db.delete(CATEGORIES_TABLE, whereClause, whereArgs)
        db.close()

        return result > 0
    }

    /*---------------Category Method--------------------------*/
    // Add Product
    fun insertProduct (product: Products) : Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(PRODUCT_COLUMN_NAME, product.name)
            put(PRODUCT_COLUMN_PRICE, product.price)
            put(PRODUCT_COLUMN_IMAGE, product.imageResId)
            put(PRODUCT_COLUMN_CATEGORY, product.category)
        }
        val result = db.insert(PRODUCTS_TABLE, null, values)
        db.close()

        return result != -1L
    }

    // Get All Product
    fun getProduct() : List<Products> {
        val productList = mutableListOf<Products>()
        val db = readableDatabase
        val query = "SELECT * FROM $PRODUCTS_TABLE"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(PRODUCT_COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(PRODUCT_COLUMN_NAME))
            val price = cursor.getInt(cursor.getColumnIndexOrThrow(PRODUCT_COLUMN_PRICE))
            val image = cursor.getString(cursor.getColumnIndexOrThrow(PRODUCT_COLUMN_IMAGE))
            val catId = cursor.getInt(cursor.getColumnIndexOrThrow(PRODUCT_COLUMN_CATEGORY))

            val cat = Products(id, name, price, image, catId)
            productList.add(cat)
        }

        cursor.close()
        db.close()

        return productList
    }

    // Get Details Product by ID
    fun getProductByID (productId: Int) : Products {
        val db = readableDatabase
        val query = "SELECT * FROM $PRODUCTS_TABLE WHERE $PRODUCT_COLUMN_ID = $productId"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()

        val id = cursor.getInt(cursor.getColumnIndexOrThrow(PRODUCT_COLUMN_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(PRODUCT_COLUMN_NAME))
        val price = cursor.getInt(cursor.getColumnIndexOrThrow(PRODUCT_COLUMN_PRICE))
        val image = cursor.getString(cursor.getColumnIndexOrThrow(PRODUCT_COLUMN_IMAGE))
        val catId = cursor.getInt(cursor.getColumnIndexOrThrow(PRODUCT_COLUMN_CATEGORY))

        cursor.close()
        db.close()
        return Products(id, name, price, image, catId)
    }

    // Update Product
    fun updateProduct (product: Products) : Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(PRODUCT_COLUMN_NAME, product.name)
            put(PRODUCT_COLUMN_PRICE, product.price)
            put(PRODUCT_COLUMN_IMAGE, product.imageResId)
            put(PRODUCT_COLUMN_CATEGORY, product.category)
        }
        val whereClause = "$PRODUCT_COLUMN_ID = ?"
        val whereArgs = arrayOf(product.id.toString())
        val result = db.update(PRODUCTS_TABLE, values, whereClause, whereArgs)
        db.close()

        return result > 0
    }

    // Delete Product
    fun deleteProduct (productId: Int) : Boolean {
        val db = writableDatabase
        val whereClause = "$PRODUCT_COLUMN_ID = ?"
        val whereArgs = arrayOf(productId.toString())
        val result = db.delete(PRODUCTS_TABLE, whereClause, whereArgs)
        db.close()

        return result > 0
    }

    /*---------------Order Method--------------------------*/
    // Add Order
    fun insertOrder (orders: Orders) : Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(ORDER_TOTAL_PRICE, orders.totalPrice)
            put(ORDER_USER, orders.userId)
            put(ORDER_NOTES, orders.notes)
        }
        val result = db.insert(ORDERS_TABLE, null, values)
        db.close()

        return result != -1L
    }

    // Update Order
    fun updateOrder (orders: Orders) : Boolean {
        val db = writableDatabase
        val query = "SELECT $ORDER_DATE FROM $ORDERS_TABLE WHERE $ORDER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(orders.id.toString()))

        if (cursor.moveToFirst()) {
            val orderDate = cursor.getString(cursor.getColumnIndexOrThrow(ORDER_DATE))
            if (isPastDate(orderDate)) {
                cursor.close()
                db.close()
                return false   // Cannot update past orders
            }
        }

        cursor.close()
        val values = ContentValues().apply {
            put(ORDER_TOTAL_PRICE, orders.totalPrice)
            put(ORDER_USER, orders.userId)
            put(ORDER_NOTES, orders.notes)
        }
        val whereClause = "$ORDER_ID = ?"
        val whereArgs = arrayOf(orders.id.toString())
        val result = db.update(ORDERS_TABLE, values, whereClause, whereArgs)
        db.close()

        return result > 0
    }

    // Delete Order
    fun deleteOrder (orderId: Int) : Boolean {
        val db = writableDatabase
        val query = "SELECT $ORDER_DATE FROM $ORDERS_TABLE WHERE $ORDER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(orderId.toString()))

        if (cursor.moveToFirst()) {
            val orderDate = cursor.getString(cursor.getColumnIndexOrThrow(ORDER_DATE))
            if (isPastDate(orderDate)) {
                cursor.close()
                db.close()
                return false   // Cannot delete past orders
            }
        }

        cursor.close()
        val whereClause = "$ORDER_ID = ?"
        val whereArgs = arrayOf(orderId.toString())
        val result = db.delete(ORDERS_TABLE, whereClause, whereArgs)
        db.close()

        return result > 0
    }

    // Check if Date Order is in the past
    private fun isPastDate (date: String) : Boolean {
        val currentTime = System.currentTimeMillis()
        val orderTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)?.time ?: return true
        return orderTime < currentTime
    }

}