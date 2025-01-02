package com.example.posapp.activities

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.posapp.Databases
import com.example.posapp.adapters.OrderCartAdapter
import com.example.posapp.databinding.ActivityOrderBinding
import com.example.posapp.models.Products
import java.text.NumberFormat
import java.util.Locale

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private lateinit var databases: Databases
    private lateinit var orderCartAdapter: OrderCartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databases = Databases(this)

        // Get cartList from intent
        val cartList = intent.getSerializableExtra("cartList") as? HashMap<Products, Int>
        if (cartList == null || cartList.isEmpty()) {
            Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setUpRecyclerView(cartList)

        val totalPrice = totalPriceOrder(cartList)
        binding.totalOrder.text = formatCurrency(totalPrice)

        binding.orderBackCashierBtn.setOnClickListener {
            finish()
        }

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

    // Caculate Total Price Order
    private fun totalPriceOrder(cartList: HashMap<Products, Int>) : Int {
        var total = 0
        for ((product, quantity) in cartList) {
            total += product.price * quantity
        }
        return total
    }

    // Place Order Function
    private fun placeOrder(cartList: HashMap<Products, Int>, totalPrice: Int) {
        val orderId = databases.insertOrder(totalPrice)
        if (orderId > 0) {
            var allDetailsInsert = true
            for ((product, quantity) in cartList) {
                var subTotal = 0
                subTotal = product.price * quantity
                val detailInsert = databases.insertOrderDetails(orderId, product.id, product.price, quantity, subTotal)
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
}