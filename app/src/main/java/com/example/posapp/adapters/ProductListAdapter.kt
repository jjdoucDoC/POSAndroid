package com.example.posapp.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.models.Products
import com.example.posapp.databinding.ListItemProductBinding
import java.text.NumberFormat
import java.util.Locale

class ProductListAdapter(
    private val context: Context,
    private val productList: List<Products>,
    private val onListAddToCart: (Products, View) -> Unit
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: ListItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Products) {
            binding.listName.text = product.name
            binding.listPrice.text = formatCurrency(product.price)
            val bitmap = BitmapFactory.decodeFile(product.imageResId)
            binding.listImage.setImageBitmap(bitmap)

            binding.listAddToCart.setOnClickListener {
                onListAddToCart(product, binding.listAddToCart)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ListItemProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    // Hàm định dạng giá tiền
    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }
}