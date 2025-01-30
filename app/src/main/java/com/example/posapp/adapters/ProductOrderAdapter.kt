package com.example.posapp.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.databinding.ListItemProductBinding
import com.example.posapp.models.Products
import java.text.NumberFormat
import java.util.Locale

class ProductOrderAdapter(
    private val context: Context,
    private var productList: List<Products>,
    private val onAddProductToOrder: (Products) -> Unit
) : RecyclerView.Adapter<ProductOrderAdapter.ProductOrderViewHolder>() {

    inner class ProductOrderViewHolder(
        private val binding: ListItemProductBinding,
        private val onAddProductToOrder: (Products) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Products) {
            binding.listName.text = product.name
            binding.listPrice.text = formatCurrency(product.price)
            val bitmap = BitmapFactory.decodeFile(product.imageResId)
            binding.listImage.setImageBitmap(bitmap)

            binding.listAddToCart.setOnClickListener {
                onAddProductToOrder(product)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductOrderViewHolder {
        val binding = ListItemProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return ProductOrderViewHolder(binding, onAddProductToOrder)
    }

    override fun onBindViewHolder(
        holder: ProductOrderViewHolder,
        position: Int
    ) {
        holder.bind(productList[position])
    }

    override fun getItemCount(): Int = productList.size

    // Hàm định dạng giá tiền
    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }

    fun updateList(newList: List<Products>) {
        productList = newList
        notifyDataSetChanged()
    }
}
