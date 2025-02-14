package com.example.posapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.Databases
import com.example.posapp.databinding.ProductEditOrderItemBinding
import com.example.posapp.models.OrderDetail
import com.example.posapp.repository.ProductRepository
import java.text.NumberFormat
import java.util.Locale

class OrderDetailAdapter (
    private val context: Context,
    private val orderDetailList: MutableList<OrderDetail>,
    private val onQuantityChanged: (OrderDetail) -> Unit    // Callback khi so luong thay doi
) : RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder>() {
    private val productRepository : ProductRepository = ProductRepository.getInstance(context)

    class OrderDetailViewHolder (private val binding: ProductEditOrderItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
        val productName = binding.productNameEditOrder
        val productImg = binding.productImageEditOrder
        val quantity = binding.quantityEditOrder
        val subTotal = binding.productSubtotalEditOrder
        val decreaseButton = binding.editDecreaseQuantityBtn
        val increaseButton = binding.editIncreaseQuantityBtn
        val removeProduct = binding.removeProductOrderDetailBtn
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderDetailAdapter.OrderDetailViewHolder {
        val binding = ProductEditOrderItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return OrderDetailViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrderDetailAdapter.OrderDetailViewHolder, position: Int) {
        val item = orderDetailList[position]

        val product = productRepository.getProductByID(item.productId)
        holder.productName.text = product.name

        val bitmap = BitmapFactory.decodeFile(product.imageResId)
        holder.productImg.setImageBitmap(bitmap)

        holder.quantity.text = "${item.quantity}"
        holder.subTotal.text =  formatCurrency(item.subTotal)

        holder.decreaseButton.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity -= 1
                item.subTotal = item.quantity * item.productPrice
                holder.quantity.text = "${item.quantity}"
                holder.subTotal.text = formatCurrency(item.subTotal)
                onQuantityChanged(item) // Gọi callback
            } else {
                orderDetailList.removeAt(position)
                notifyItemRemoved(position)
                onQuantityChanged(item)
            }
        }

        holder.increaseButton.setOnClickListener {
            item.quantity += 1
            item.subTotal = item.quantity * item.productPrice
            holder.quantity.text = "${item.quantity}"
            holder.subTotal.text = formatCurrency(item.subTotal)
            onQuantityChanged(item) // Gọi callback
        }

        holder.removeProduct.setOnClickListener {
            orderDetailList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, orderDetailList.size)
            onQuantityChanged(item)
        }
    }

    override fun getItemCount(): Int {
        return orderDetailList.size
    }

    // Hàm định dạng giá tiền
    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }
}