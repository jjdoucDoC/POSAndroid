package com.example.posapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.Databases
import com.example.posapp.R
import com.example.posapp.adapters.OrderDetailAdapter
import com.example.posapp.adapters.ProductListAdapter
import com.example.posapp.adapters.ProductOrderAdapter
import com.example.posapp.databinding.ActivityEditOrderBinding
import com.example.posapp.models.OrderDetail
import com.example.posapp.models.Orders
import com.example.posapp.models.Products
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.NumberFormat
import java.util.Locale

class EditOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditOrderBinding
    private lateinit var databases: Databases

    private lateinit var orderDetailAdapter: OrderDetailAdapter
    private var orderId: Int = -1
    private var orderDetailList: MutableList<OrderDetail> = mutableListOf()
    private lateinit var productList: List<Products>

    private val REQUEST_CUSTOMER = 1001

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databases = Databases(this)

        orderId = intent.getIntExtra("orderId", -1)
        if (orderId == -1) {
            finish()
            return
        }

        setUpRecyclerView()
        fetchOrderDetails()
        loadCustomerData()

        binding.customerDetail.setOnClickListener {
            val intent = Intent(this, CustomerActivity::class.java)

            val name = binding.customerNameEditOrder.text.toString().trim()
            val phone = binding.customerPhoneEditOrder.text.toString().trim()
            val address = binding.customerAddressEditOrder.text.toString().trim()

            if (name.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty()) {
                intent.putExtra("customerName", name)
                intent.putExtra("customerPhone", phone)
                intent.putExtra("customerAddress", address)
            }

            startActivityForResult(intent, REQUEST_CUSTOMER)
        }

        binding.editDeliveryDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            // Show DatePickerDialog
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val timeCalendar = Calendar.getInstance()
                    timeCalendar.set(year, month, dayOfMonth)
                    // Refresh TextView
                    val selectedDate = String.format(
                        "%04d-%02d-%02d",
                        year, month + 1, dayOfMonth
                    )
                    binding.editDeliveryDate.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.addNewProductOrder.setOnClickListener {
            showProductBottomSheet()
        }

        binding.updateOrderBtn.setOnClickListener {
            if(!isOrderEditable()) {
                Toast.makeText(this, "Cannot edit delivered order!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(orderDetailList.isEmpty()) {
                Toast.makeText(this, "Cannot update order with no products!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            updateOrder()
        }

        binding.deleteOrderBtn.setOnClickListener {
            if(!isOrderEditable()) {
                Toast.makeText(this, "Cannot delete delivered order!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val isDeleted = databases.deleteOrder(orderId)
            if (isDeleted) {
                Toast.makeText(this, "Order deleted successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to delete category!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.orderEditBackBtn.setOnClickListener {
            finish()
        }

    }

    private fun loadCustomerData() {
        val order = databases.getOrderByID(orderId)
        binding.customerNameEditOrder.setText(order.customerName)
        binding.customerPhoneEditOrder.setText(order.customerPhone)
        binding.customerAddressEditOrder.setText(order.customerAddress)
        binding.editDeliveryDate.setText(order.deliveryDate)
        binding.editOrderNoteInput.setText(order.notes)
    }

    private fun setUpRecyclerView() {
        orderDetailAdapter = OrderDetailAdapter(this, orderDetailList) { updateOrderDetail ->
            updateTotalPrice()
        }
        binding.orderDetailList.apply {
            layoutManager = LinearLayoutManager(this@EditOrderActivity)
            adapter = orderDetailAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchOrderDetails() {
        try {
            val orderDetails = databases.getOrderDetailByOrderID(orderId)
            orderDetailList.apply {
                clear()
                addAll(orderDetails)
            }
            orderDetailAdapter.notifyDataSetChanged()
            updateTotalPrice()
        } catch (e: Exception) {
            Log.e("EditOrderActivity", "Error fetching order details: ${e.message}", e)
        }
    }

    private fun updateTotalPrice() {
        // Tính tổng giá trị đơn hàng
        val totalPrice = orderDetailList.sumOf { it.subTotal }
        binding.newTotalPrice.text = formatCurrency(totalPrice)
    }

    private fun showProductBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_product, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val productRecyclerView =
            bottomSheetView.findViewById<RecyclerView>(R.id.newProduct_cart_list)
        val searchProductInput = bottomSheetView.findViewById<EditText>(R.id.search_productOrder)

        productRecyclerView.layoutManager = LinearLayoutManager(this)

        // Lấy danh sách sản phẩm từ cơ sở dữ liệu
        productList = databases.getProduct()

        val productAdapter = ProductOrderAdapter(this, productList) { selectedProduct ->
            // Kiểm tra nếu sản phẩm đã có trong danh sách đơn hàng
            val isAlreadyInOrder = orderDetailList.any { it.productId == selectedProduct.id }
            if (isAlreadyInOrder) {
                Toast.makeText(this, "Product already exists in order!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Thêm sản phẩm vào danh sách đơn hàng
                val newOrderDetail = OrderDetail(
                    id = 0,
                    orderId = orderId,
                    productId = selectedProduct.id,
                    productPrice = selectedProduct.price,
                    quantity = 1,
                    subTotal = selectedProduct.price
                )
                orderDetailList.add(newOrderDetail)
                orderDetailAdapter.notifyDataSetChanged()
                updateTotalPrice()
                bottomSheetDialog.dismiss()
            }
        }

        productRecyclerView.adapter = productAdapter

        // Thêm sự kiện lắng nghe thay đổi văn bản
        searchProductInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase(Locale.getDefault())
                val filteredList = databases.getProduct().filter {
                    it.name.lowercase(Locale.getDefault()).contains(query) ||
                            it.id.toString().contains(query)
                }
                productAdapter.updateList(filteredList)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        bottomSheetDialog.show()
    }

    private fun isOrderEditable() : Boolean {
        val order = databases.getOrderByID(orderId)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val deliveryDate = sdf.parse(order.deliveryDate)
        val currentDate = java.util.Calendar.getInstance().time

        // Nếu ngày giao hàng nhỏ hơn ngày hiện tại hoặc trạng thái là "Delivered" thì không cho sửa/xóa
        return !(currentDate.after(deliveryDate) || order.status == "Delivered")
    }

    private fun updateOrder() {
        // Get customer info
        val name = binding.customerNameEditOrder.text.toString().trim()
        val phone = binding.customerPhoneEditOrder.text.toString().trim()
        val address = binding.customerAddressEditOrder.text.toString().trim()
        if (name.isEmpty() && phone.isEmpty() && address.isEmpty()) {
            Toast.makeText(this, "Please provide customer information!", Toast.LENGTH_SHORT).show()
            return
        }

        val deliveryDateStr = binding.editDeliveryDate.text.toString().trim()
        if (deliveryDateStr.isEmpty()) {
            Toast.makeText(this, "Please select a delivery date.", Toast.LENGTH_SHORT).show()
            return
        }

        // Xác định trạng thái đơn hàng
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val deliveryDate = sdf.parse(deliveryDateStr)
        val currentDate = java.util.Calendar.getInstance().time
        val orderStatus = if (currentDate.after(deliveryDate)) {
            "Delivered"
        } else {
            "Shipping"
        }

        // Lấy userId
        val getOrderId = databases.getOrderByID(orderId)
        val userId = getOrderId.userId
        val orderDate = getOrderId.orderDate
        val orderNotes = binding.editOrderNoteInput.text.toString().trim()

        // Tính lại tổng giá trị đơn hàng
        val totalPrice = orderDetailList.sumOf { it.subTotal }

        val order = Orders(
            id = orderId,
            totalPrice = totalPrice,
            deliveryDate = deliveryDateStr,
            userId = userId,
            notes = orderNotes,
            customerName = name,
            customerPhone = phone,
            customerAddress = address,
            status = orderStatus,
            orderDate = orderDate
        )
        val isOrderUpdated = databases.updateOrder(order)
        if (!isOrderUpdated) {
            Toast.makeText(this, "Order updated fail!", Toast.LENGTH_SHORT).show()
            return
        }

        // Xóa chi tiết đơn hàng cũ
        val isDetailsDeleted = databases.deleteOrderDetailsByOrderID(orderId)

        if (!isDetailsDeleted) {
            Toast.makeText(this, "Error to delete products!", Toast.LENGTH_SHORT).show()
            return
        }

        // Thêm lại danh sách sản phẩm mới vào đơn hàng
        var allInserted = true
        for (detail in orderDetailList) {
            val newDetail = OrderDetail(
                id = 0,
                orderId = orderId,
                productId = detail.productId,
                productPrice = detail.productPrice,
                quantity = detail.quantity,
                subTotal = detail.subTotal
            )
            val isInserted = databases.insertOrderDetails(newDetail)
            if (!isInserted) {
                allInserted = false
            }
        }

        if (allInserted) {
            Toast.makeText(this, "Order updated successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error to updating order!", Toast.LENGTH_SHORT).show()
        }

    }

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
                binding.customerNameEditOrder.text = "$customerName"
                binding.customerPhoneEditOrder.text = "$customerPhone"
                binding.customerAddressEditOrder.text = "$customerAddress"
            }
        }
    }

}