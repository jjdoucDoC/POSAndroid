package com.example.posapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.databinding.OrderItemBinding
import com.example.posapp.models.Orders
import java.text.NumberFormat
import java.util.Locale

class OrderAdapter(
    private val context: Context,
    private var orderList: List<Orders>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {
    class OrderViewHolder(private val binding: OrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val orderPrice = binding.orderPrice
        val orderDate = binding.orderDate
        val orderId = binding.orderId
        val status = binding.orderStatus
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
        holder.status.text = "Shipping"
    }

    // Format Currency Function
    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }
}