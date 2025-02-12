package com.example.posapp.repository

import android.content.ContentValues
import android.content.Context
import com.example.posapp.Databases
import com.example.posapp.Databases.Companion.COLUMN_ORDER_ID_FK
import com.example.posapp.Databases.Companion.COLUMN_PRODUCT_PRICE
import com.example.posapp.Databases.Companion.DELIVERY_DATE
import com.example.posapp.Databases.Companion.ORDERS_TABLE
import com.example.posapp.Databases.Companion.ORDER_CUSTOMER_ADDRESS
import com.example.posapp.Databases.Companion.ORDER_CUSTOMER_NAME
import com.example.posapp.Databases.Companion.ORDER_CUSTOMER_PHONE
import com.example.posapp.Databases.Companion.ORDER_DATE
import com.example.posapp.Databases.Companion.ORDER_DETAILS_ID
import com.example.posapp.Databases.Companion.ORDER_DETAILS_PRODUCT
import com.example.posapp.Databases.Companion.ORDER_DETAILS_QUANTITY
import com.example.posapp.Databases.Companion.ORDER_DETAILS_TABLE
import com.example.posapp.Databases.Companion.ORDER_DETAILS_TOTAL_PRICE
import com.example.posapp.Databases.Companion.ORDER_ID
import com.example.posapp.Databases.Companion.ORDER_NOTES
import com.example.posapp.Databases.Companion.ORDER_STATUS
import com.example.posapp.Databases.Companion.ORDER_TOTAL_PRICE
import com.example.posapp.Databases.Companion.ORDER_USER
import com.example.posapp.models.OrderDetail
import com.example.posapp.models.Orders

class OrderRepository(private val db: Databases) {
    companion object {
        var instance: OrderRepository? = null
        @Synchronized
        fun getInstance(context: Context): OrderRepository {
            return if (instance == null) {
                OrderRepository(Databases.getInstance(context)).also {
                    instance = it
                }
            } else {
                instance!!
            }
        }
    }

    // Add Order
    fun insertOrder(order: Orders): Long {
        val db = db.writableDatabase
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
        val db = db.writableDatabase
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
        val db = db.readableDatabase
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
        val db = db.readableDatabase
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
        val db = db.readableDatabase
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
        val db = db.readableDatabase
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
        val db = db.readableDatabase
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

    // Update Order Status
    fun updateOrderStatus(orderID: Int, newStatus: String): Boolean {
        val db = db.writableDatabase
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
        val db = db.writableDatabase
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
        val db = db.writableDatabase
        val whereClause = "$ORDER_ID = ?"
        val whereArgs = arrayOf(orderID.toString())
        val result = db.delete(ORDERS_TABLE, whereClause, whereArgs)
        db.close()

        return result > 0
    }

    // Delete Order Detail
    fun deleteOrderDetailsByOrderID(orderID: Int): Boolean {
        val db = db.writableDatabase
        val whereClause = "$COLUMN_ORDER_ID_FK = ?"
        val whereArgs = arrayOf(orderID.toString())
        val result = db.delete(ORDER_DETAILS_TABLE, whereClause, whereArgs)
        db.close()

        return result > 0
    }
}