package com.example.posapp.repository

import android.content.ContentValues
import android.content.Context
import android.icu.util.Calendar
import com.example.posapp.Databases
import com.example.posapp.Databases.Companion.COLUMN_ORDER_ID_FK
import com.example.posapp.Databases.Companion.ORDERS_TABLE
import com.example.posapp.Databases.Companion.ORDER_DATE
import com.example.posapp.Databases.Companion.ORDER_DETAILS_PRODUCT
import com.example.posapp.Databases.Companion.ORDER_DETAILS_QUANTITY
import com.example.posapp.Databases.Companion.ORDER_DETAILS_TABLE
import com.example.posapp.Databases.Companion.ORDER_DETAILS_TOTAL_PRICE
import com.example.posapp.Databases.Companion.ORDER_ID
import com.example.posapp.Databases.Companion.PRODUCTS_TABLE
import com.example.posapp.Databases.Companion.PRODUCT_COLUMN_CATEGORY
import com.example.posapp.Databases.Companion.PRODUCT_COLUMN_ID
import com.example.posapp.Databases.Companion.PRODUCT_COLUMN_IMAGE
import com.example.posapp.Databases.Companion.PRODUCT_COLUMN_NAME
import com.example.posapp.Databases.Companion.PRODUCT_COLUMN_PRICE
import com.example.posapp.models.OrderDetail
import com.example.posapp.models.Products
import java.text.SimpleDateFormat
import java.util.Locale

class ProductRepository(private val db: Databases) {
    companion object {
        var instance: ProductRepository? = null
        @Synchronized
        fun getInstance(context: Context): ProductRepository {
            return if (instance == null) {
                ProductRepository(Databases.getInstance(context)).also {
                    instance = it
                }
            } else {
                instance!!
            }
        }
    }

    // Add Product
    fun insertProduct(product: Products): Boolean {
        val db = db.writableDatabase
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
        val db = db.readableDatabase
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
        val db = db.readableDatabase
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
        val db = db.writableDatabase
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
        val db = db.writableDatabase
        val whereClause = "$PRODUCT_COLUMN_ID = ?"
        val whereArgs = arrayOf(productId.toString())
        val result = db.delete(PRODUCTS_TABLE, whereClause, whereArgs)
        db.close()

        return result > 0
    }

    // Get Best Selling Product
    fun getBestSellingProducts(timeRange: String): List<OrderDetail> {
        val db = db.readableDatabase
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
}