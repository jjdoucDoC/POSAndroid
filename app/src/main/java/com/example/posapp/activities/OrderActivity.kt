package com.example.posapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.posapp.Databases
import com.example.posapp.adapters.OrderCartAdapter
import com.example.posapp.databinding.ActivityOrderBinding
import com.example.posapp.models.OrderDetail
import com.example.posapp.models.Orders
import com.example.posapp.models.Products
import com.example.posapp.repository.OrderRepository
import java.text.NumberFormat
import java.util.Locale

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private lateinit var orderRepository: OrderRepository
    private lateinit var orderCartAdapter: OrderCartAdapter

    private val REQUEST_CUSTOMER = 1001

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderRepository = OrderRepository.getInstance(this)

        // Get cartList data from intent Cashier
        val cartList = intent.getSerializableExtra("cartList") as? HashMap<Products, Int>
        if (cartList.isNullOrEmpty()) {
            Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setUpRecyclerView(cartList)

        binding.orderBackCashierBtn.setOnClickListener {
            finish()
        }

        // Edit Customer Information
        binding.customer.setOnClickListener {
            val intent = Intent(this, CustomerActivity::class.java)

            // Pass existing customer data to CustomerActivity
            val name = binding.customerName.text.toString().trim()
            val phone = binding.customerPhone.text.toString().trim()
            val address = binding.customerAddress.text.toString().trim()

            if (name.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty()) {
                intent.putExtra("customerName", name)
                intent.putExtra("customerPhone", phone)
                intent.putExtra("customerAddress", address)
            }

            startActivityForResult(intent, REQUEST_CUSTOMER)
        }

        // Choose Delivery Date Button
        binding.chooseDeliveryDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            // Show DatePickerDialog
            DatePickerDialog(this, {_, year, month, dayOfMonth ->
                val timeCalendar = Calendar.getInstance()
                timeCalendar.set(year, month, dayOfMonth)
                    // Refresh TextView
                    val selectedDate = String.format(
                        "%04d-%02d-%02d",
                        year, month + 1, dayOfMonth
                    )
                    binding.chooseDeliveryDate.text = selectedDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val totalPrice = totalPriceOrder(cartList)
        binding.totalPrice.text = formatCurrency(totalPrice)

        binding.placeOrderBtn.setOnClickListener {
            placeOrder(cartList, totalPrice)
        }
    }

    // Set up Recycler View
    private fun setUpRecyclerView(cartList: HashMap<Products, Int>) {
        orderCartAdapter = OrderCartAdapter(cartList)
        binding.orderCartList.apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
            adapter = orderCartAdapter
        }
    }

    // Calculate Total Price Order
    private fun totalPriceOrder(cartList: HashMap<Products, Int>) : Int {
        var total = 0
        for ((product, quantity) in cartList) {
            total += product.price * quantity
        }
        return total
    }

    // Place Order Function
    private fun placeOrder(cartList: HashMap<Products, Int>, totalPrice: Int) {
        val deliveryDateStr = binding.chooseDeliveryDate.text.toString().trim()
        if (deliveryDateStr.isEmpty()) {
            Toast.makeText(this, "Please select a delivery date.", Toast.LENGTH_SHORT).show()
            return
        }

        // Parse the delivery date
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val deliveryDate = sdf.parse(deliveryDateStr)

        // Get the current date
        val currentDate = java.util.Calendar.getInstance().time

        // Xác định trạng thái đơn hàng
        val orderStatus = if (currentDate.after(deliveryDate)) {
            "Delivered"
        } else {
            "Shipping"
        }

        // Get user ID
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Get customer info
        val name = binding.customerName.text.toString().trim()
        val phone = binding.customerPhone.text.toString().trim()
        val address = binding.customerAddress.text.toString().trim()
        if (name.isEmpty() && phone.isEmpty() && address.isEmpty()) {
            Toast.makeText(this, "Please provide customer information!", Toast.LENGTH_SHORT).show()
            return
        }

        val notes = binding.addOrderNoteInput.text.toString().trim()

        val order = Orders(
            id = 0,
            totalPrice = totalPrice,
            deliveryDate = deliveryDateStr,
            userId = userId,
            notes = notes,
            customerName = name,
            customerPhone = phone,
            customerAddress = address,
            status = orderStatus
        )
        val orderId = orderRepository.insertOrder(order)
        if (orderId > 0) {
            var allDetailsInsert = true
            for ((product, quantity) in cartList) {
                var subTotal = 0
                subTotal = product.price * quantity
                val orderDetail = OrderDetail(
                    id = 0,
                    orderId = orderId.toInt(),
                    productId = product.id,
                    productPrice = product.price,
                    quantity = quantity,
                    subTotal = subTotal
                )
                val detailInsert = orderRepository.insertOrderDetails(orderDetail)
                if (!detailInsert) {
                    allDetailsInsert = false
                    break
                }
            }

            if (allDetailsInsert) {
                // Set cartList empty when return Cashier screen
                val resultIntent = intent
                resultIntent.putExtra("clearCart", true)
                setResult(Activity.RESULT_OK, resultIntent)

                Toast.makeText(this, "Place order successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error adding order details. Please try again!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Place order failed. Please try again!", Toast.LENGTH_SHORT).show()
        }
    }

    // Hàm định dạng giá tiền
    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CUSTOMER && resultCode == Activity.RESULT_OK) {
            val customerName = data?.getStringExtra("customerName")
            val customerPhone = data?.getStringExtra("customerPhone")
            val customerAddress = data?.getStringExtra("customerAddress")

            if (customerName != null && customerPhone != null && customerAddress != null) {
                binding.customerName.text = "$customerName"
                binding.customerPhone.text = "$customerPhone"
                binding.customerAddress.text = "$customerAddress"
                binding.customerInfoLayout.visibility = View.VISIBLE
            }
        }
    }

}