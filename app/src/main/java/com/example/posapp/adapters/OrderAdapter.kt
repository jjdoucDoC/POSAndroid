package com.example.posapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.Databases
import com.example.posapp.R
import com.example.posapp.activities.EditOrderActivity
import com.example.posapp.databinding.OrderItemBinding
import com.example.posapp.models.Categories
import com.example.posapp.models.Orders
import com.example.posapp.repository.OrderRepository
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private val context: Context,
    private var orderList: List<Orders>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {
    class OrderViewHolder(val binding: OrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val orderPrice = binding.orderPrice
        val orderDate = binding.orderDate
        val orderId = binding.orderId
        val status = binding.orderStatus
        val cardViewStatus = binding.statusCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = OrderItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = orderList[position]
        holder.orderPrice.text = formatCurrency(item.totalPrice)
        holder.orderDate.text = item.orderDate
        holder.orderId.text = "# " + "${position + 1}"
        holder.status.text = item.status

        // Kiểm tra nếu ngày hiện tại nhỏ hơn ngày giao hàng
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val deliveryDate = sdf.parse(item.deliveryDate)

        val currentDate = java.util.Calendar.getInstance().time

        if (currentDate.after(deliveryDate)) {
            updateOrderStatus(item.id, "Delivered")
            holder.status.text = "Delivered"
        } else {
            updateOrderStatus(item.id, "Shipping")
            holder.status.text = "Shipping"
        }

        if (item.status == "Delivered") {
            holder.cardViewStatus.setCardBackgroundColor(Color.parseColor("#39AD3F"))
        } else {
            holder.cardViewStatus.setCardBackgroundColor(Color.parseColor("#1A72DD"))
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, EditOrderActivity::class.java).apply {
                putExtra("orderId", item.id)
            }
            context.startActivity(intent)
        }


    }

    // Update Order Status From Database
    private fun updateOrderStatus(orderId: Int, newStatus: String) {
        OrderRepository.getInstance(context).updateOrderStatus(orderId, newStatus)
    }

    // Format Currency Function
    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }


    // Làm mới danh sách loại sản phẩm
    fun refreshData(newOrders: List<Orders>) {
        orderList = newOrders
        notifyDataSetChanged()
    }
}