package com.example.posapp.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.databinding.ProductOrderItemBinding
import com.example.posapp.models.Products
import java.text.NumberFormat
import java.util.Locale

class OrderCartAdapter(
    private val cartList: HashMap<Products, Int>
) : RecyclerView.Adapter<OrderCartAdapter.OrderCartViewHolder>() {

    private val productList = cartList.keys.toList()

    class OrderCartViewHolder(private val binding: ProductOrderItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val productName = binding.productNameOrder
        val productImg = binding.productImageOrder
        val quantity = binding.productQuantityOrder
        val subTotal = binding.productSubtotalOrder
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderCartViewHolder {
        val binding = ProductOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderCartViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: OrderCartViewHolder, position: Int) {
        val item = productList[position]
        val quantity = cartList[item] ?: 0

        holder.productName.text = item.name

        val bitmap = BitmapFactory.decodeFile(item.imageResId)
        holder.productImg.setImageBitmap(bitmap)

        holder.quantity.text = "$quantity"

        val itemSubtotal = item.price * quantity
        holder.subTotal.text = formatCurrency(itemSubtotal)
    }

    // Hàm định dạng giá tiền
    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }
}