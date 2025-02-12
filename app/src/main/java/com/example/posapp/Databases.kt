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

    /*---------------User Method--------------------------*/

    // Check Valid Login
    @SuppressLint("Range")
    fun isValidUser(emailOrPhone: String, password: String): Int {
        val db = readableDatabase
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
    fun insertCategory(category: Categories): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(CATEGORY_COLUMN_NAME, category.name)
        }
        val result = db.insert(CATEGORIES_TABLE, null, values)
        db.close()

        return result != -1L
    }

    // Get All Category
    fun getCategory(): List<Categories> {
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
        val query =
            "SELECT $CATEGORY_COLUMN_ID FROM $CATEGORIES_TABLE WHERE $CATEGORY_COLUMN_NAME = ?"
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
    fun getCategoryByID(categoryId: Int): Categories {
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
    fun updateCategory(category: Categories): Boolean {
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
    fun deleteCategory(categoryId: Int): Boolean {
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
    fun insertProduct(product: Products): Boolean {
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
    fun getProduct(): List<Products> {
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

            val pro = Products(id, name, price, image, catId)
            productList.add(pro)
        }

        cursor.close()
        db.close()

        return productList
    }

    // Get Details Product by ID
    fun getProductByID(productId: Int): Products {
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
    fun updateProduct(product: Products): Boolean {
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
    fun deleteProduct(productId: Int): Boolean {
        val db = writableDatabase
        val whereClause = "$PRODUCT_COLUMN_ID = ?"
        val whereArgs = arrayOf(productId.toString())
        val result = db.delete(PRODUCTS_TABLE, whereClause, whereArgs)
        db.close()

        return result > 0
    }

    /*---------------Order Method--------------------------*/
    // Add Order
    fun insertOrder(order: Orders): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(ORDER_TOTAL_PRICE, order.totalPrice)
            put(ORDER_DATE, order.orderDate)
            put(DELIVERY_DATE, order.deliveryDate)
            put(ORDER_USER, order.userId)
            put(ORDER_NOTES, order.notes ?: "") // if notes is null, insert empty string
            put(ORDER_CUSTOMER_NAME, order.customerName)
            put(ORDER_CUSTOMER_PHONE, order.customerPhone)
            put(ORDER_CUSTOMER_ADDRESS, order.customerAddress)
            put(ORDER_STATUS, order.status)
        }
        return db.insert(ORDERS_TABLE, null, values)
    }

    // Add Order Detail
    fun insertOrderDetails(orderDetail: OrderDetail): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ORDER_ID_FK, orderDetail.orderId)
            put(ORDER_DETAILS_PRODUCT, orderDetail.productId)
            put(COLUMN_PRODUCT_PRICE, orderDetail.productPrice)
            put(ORDER_DETAILS_QUANTITY, orderDetail.quantity)
            put(ORDER_DETAILS_TOTAL_PRICE, orderDetail.subTotal)
        }
        val result = db.insert(ORDER_DETAILS_TABLE, null, values)
        return result != -1L
    }

    // Get All Order
    fun getOrder(): List<Orders> {
        val orderList = mutableListOf<Orders>()
        val db = readableDatabase
        val query = "SELECT * FROM $ORDERS_TABLE GROUP BY $ORDER_ID"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_ID))
            val price = cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_TOTAL_PRICE))
            val deliveryDate = cursor.getString(cursor.getColumnIndexOrThrow(DELIVERY_DATE))
            val user = cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_USER))
            val notes = cursor.getString(cursor.getColumnIndexOrThrow(ORDER_NOTES))
            val orderDate = cursor.getString(cursor.getColumnIndexOrThrow(ORDER_DATE))
            val customerName = cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_CUSTOMER_NAME))
            val customerPhone = cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_CUSTOMER_PHONE))
            val customerAddress =
                cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_CUSTOMER_ADDRESS))
            val status = cursor.getString(cursor.getColumnIndexOrThrow(ORDER_STATUS))

            val ord = Orders(
                id,
                price,
                deliveryDate,
                user,
                notes,
                customerName.toString(),
                customerPhone.toString(),
                customerAddress.toString(),
                status,
                orderDate
            )
            orderList.add(ord)
        }

        cursor.close()
        db.close()

        return orderList
    }

    // Get Order by ID
    fun getOrderByID(orderID: Int): Orders {
        val db = readableDatabase
        val query = "SELECT * FROM $ORDERS_TABLE WHERE $ORDER_ID = $orderID"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()

        val id = cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_ID))
        val totalPrice = cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_TOTAL_PRICE))
        val orderDate = cursor.getString(cursor.getColumnIndexOrThrow(ORDER_DATE))
        val user = cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_USER))
        val deliveryDate = cursor.getString(cursor.getColumnIndexOrThrow(DELIVERY_DATE))
        val notes = cursor.getString(cursor.getColumnIndexOrThrow(ORDER_NOTES))
        val customerName = cursor.getString(cursor.getColumnIndexOrThrow(ORDER_CUSTOMER_NAME))
        val customerPhone = cursor.getString(cursor.getColumnIndexOrThrow(ORDER_CUSTOMER_PHONE))
        val customerAddress = cursor.getString(cursor.getColumnIndexOrThrow(ORDER_CUSTOMER_ADDRESS))
        val status = cursor.getString(cursor.getColumnIndexOrThrow(ORDER_STATUS))

        cursor.close()
        db.close()
        return Orders(
            id,
            totalPrice,
            deliveryDate,
            user,
            notes,
            customerName,
            customerPhone,
            customerAddress,
            status,
            orderDate
        )
    }

    // Get Order details by Order ID
    fun getOrderDetailByOrderID(orderID: Int): List<OrderDetail> {
        val db = readableDatabase
        val query = "SELECT * FROM $ORDER_DETAILS_TABLE WHERE $COLUMN_ORDER_ID_FK = ?"
        val cursor = db.rawQuery(query, arrayOf(orderID.toString()))
        val orderDetailList = mutableListOf<OrderDetail>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_DETAILS_ID))
                val productId = cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_DETAILS_PRODUCT))
                val productPrice = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_DETAILS_QUANTITY))
                val subTotal =
                    cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_DETAILS_TOTAL_PRICE))

                orderDetailList.add(
                    OrderDetail(
                        id,
                        orderID,
                        productId,
                        productPrice,
                        quantity,
                        subTotal
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return orderDetailList
    }

    // Get Order Status Count
    fun getOrderStatusCount(timeRange: String): Map<String, Int> {
        val db = readableDatabase
        val query = when (timeRange) {
            "7_days" -> "SELECT $ORDER_STATUS, COUNT(*) FROM $ORDERS_TABLE WHERE $ORDER_DATE >= date('now', '-7 days') GROUP BY $ORDER_STATUS"
            "30_days" -> "SELECT $ORDER_STATUS, COUNT(*) FROM $ORDERS_TABLE WHERE $ORDER_DATE >= date('now', '-30 days') GROUP BY $ORDER_STATUS"
            else -> "SELECT $ORDER_STATUS, COUNT(*) FROM $ORDERS_TABLE WHERE date($ORDER_DATE) = date('now') GROUP BY $ORDER_STATUS"
        }

        val cursor = db.rawQuery(query, null)
        val result = mutableMapOf<String, Int>()

        while (cursor.moveToNext()) {
            val status = cursor.getString(0) // orderDate
            val count = cursor.getInt(1)   // total_orders
            result[status] = count
        }

        cursor.close()
        db.close()
        return result
    }

    // Get Order Revenue & Order Count
    fun getTotalRevenueAndOrders(timeRange: String): Pair<Int, Int> {
        val db = readableDatabase
        var totalOrders = 0
        var totalRevenue = 0

        val query = when (timeRange) {
            "today" -> "SELECT COUNT(*) AS order_count, SUM($ORDER_TOTAL_PRICE) AS total_revenue FROM $ORDERS_TABLE WHERE DATE($ORDER_DATE) = DATE('now')"
            "7_days" -> "SELECT COUNT(*) AS order_count, SUM($ORDER_TOTAL_PRICE) AS total_revenue FROM $ORDERS_TABLE WHERE DATE($ORDER_DATE) >= DATE('now', '-6 days')"
            "30_days" -> "SELECT COUNT(*) AS order_count, SUM($ORDER_TOTAL_PRICE) AS total_revenue FROM $ORDERS_TABLE WHERE DATE($ORDER_DATE) >= DATE('now', '-29 days')"
            else -> "SELECT COUNT(*) AS order_count, SUM($ORDER_TOTAL_PRICE) AS total_revenue FROM $ORDERS_TABLE"
        }

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            totalOrders = cursor.getInt(cursor.getColumnIndexOrThrow("order_count"))
            totalRevenue = cursor.getInt(cursor.getColumnIndexOrThrow("total_revenue"))
        }

        cursor.close()
        db.close()
        return Pair(totalOrders, totalRevenue)
    }

    // Get Best Selling Product
    fun getBestSellingProducts(timeRange: String): List<OrderDetail> {
        val db = readableDatabase
        val productList = mutableListOf<OrderDetail>()

        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val (startDate, endDate) = when (timeRange) {
            "today" -> {
                val today = sdf.format(calendar.time)
                today to today
            }

            "yesterday" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = sdf.format(calendar.time)
                yesterday to yesterday
            }

            "last_7_days" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val startOfWeek = sdf.format(calendar.time)
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                val endOfWeek = sdf.format(calendar.time)
                startOfWeek to endOfWeek
            }

            "last_30_days" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startOfMonth = sdf.format(calendar.time)
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val endOfMonth = sdf.format(calendar.time)
                startOfMonth to endOfMonth
            }

            "this_year" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                val startOfYear = sdf.format(calendar.time)
                calendar.add(Calendar.YEAR, 1)
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val endOfYear = sdf.format(calendar.time)
                startOfYear to endOfYear
            }

            else -> {
                return emptyList() // Nếu `timeRange` không hợp lệ, trả về danh sách trống
            }
        }

        val query = """
                    SELECT od.$ORDER_DETAILS_PRODUCT, SUM(od.$ORDER_DETAILS_QUANTITY) AS total_sold, 
                           SUM(od.$ORDER_DETAILS_TOTAL_PRICE) AS revenue, 
                           p.$PRODUCT_COLUMN_PRICE, o.$ORDER_ID
                    FROM $ORDER_DETAILS_TABLE od
                    JOIN $ORDERS_TABLE o ON od.$COLUMN_ORDER_ID_FK = o.$ORDER_ID
                    JOIN $PRODUCTS_TABLE p ON od.$ORDER_DETAILS_PRODUCT = p.$PRODUCT_COLUMN_ID
                    WHERE DATE(o.$ORDER_DATE) BETWEEN ? AND ?
                    GROUP BY od.$ORDER_DETAILS_PRODUCT
                    ORDER BY total_sold DESC
                """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))

        while (cursor.moveToNext()) {
            val productId = cursor.getInt(0)
            val totalSold = cursor.getInt(1)
            val revenue = cursor.getInt(2)
            val productPrice = cursor.getInt(3)
            val orderId = cursor.getInt(4)

            productList.add(
                OrderDetail(
                    id = 0,
                    orderId = orderId,
                    productId = productId,
                    productPrice = productPrice,
                    quantity = totalSold,
                    subTotal = revenue
                )
            )
        }
        cursor.close()
        db.close()
        return productList
    }


    // Update Order Status
    fun updateOrderStatus(orderID: Int, newStatus: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(ORDER_STATUS, newStatus)
        }
        val whereClause = "$ORDER_ID = ?"
        val whereArgs = arrayOf(orderID.toString())
        val result = db.update(ORDERS_TABLE, values, whereClause, whereArgs)
        db.close()

        return result > 0
    }

    // Update Order
    fun updateOrder(orders: Orders): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(ORDER_TOTAL_PRICE, orders.totalPrice)
            put(DELIVERY_DATE, orders.deliveryDate)
            put(ORDER_USER, orders.userId)
            put(ORDER_NOTES, orders.notes ?: "")
            put(ORDER_CUSTOMER_NAME, orders.customerName)
            put(ORDER_CUSTOMER_PHONE, orders.customerPhone)
            put(ORDER_CUSTOMER_ADDRESS, orders.customerAddress)
            put(ORDER_STATUS, orders.status)
        }

        val whereClause = "$ORDER_ID = ?"
        val whereArgs = arrayOf(orders.id.toString())
        val result = db.update(ORDERS_TABLE, values, whereClause, whereArgs)
        db.close()

        return result > 0
    }

    // Delete Order
    fun deleteOrder(orderID: Int): Boolean {
        val db = writableDatabase
        val whereClause = "$ORDER_ID = ?"
        val whereArgs = arrayOf(orderID.toString())
        val result = db.delete(ORDERS_TABLE, whereClause, whereArgs)
        db.close()

        return result > 0
    }

    // Delete Order Detail
    fun deleteOrderDetailsByOrderID(orderID: Int): Boolean {
        val db = writableDatabase
        val whereClause = "$COLUMN_ORDER_ID_FK = ?"
        val whereArgs = arrayOf(orderID.toString())
        val result = db.delete(ORDER_DETAILS_TABLE, whereClause, whereArgs)
        db.close()

        return result > 0
    }

}