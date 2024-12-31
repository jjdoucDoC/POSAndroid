package com.example.posapp.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.Databases
import com.example.posapp.models.Products
import com.example.posapp.databinding.GridItemProductBinding
import java.text.NumberFormat
import java.util.Locale

class ProductGridAdapter(
    private val context: Context,
    private val productList: List<Products>,
    private val onGridAddToCart: (Products, View) -> Unit // Callback to handle "Add to Cart" click
) : RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: GridItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun bind(product: Products) {
                binding.gridName.text = product.name
                binding.gridPrice.text = formatCurrency(product.price)
                val bitmap = BitmapFactory.decodeFile(product.imageResId)
                binding.gridImage.setImageBitmap(bitmap)

                // Set click listener for "Add to Cart"
                binding.gridAddToCart.setOnClickListener {
                    onGridAddToCart(product, binding.gridAddToCart) // Pass product and image view to callback
                }
            }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = GridItemProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

}

    // Hàm định dạng giá tiền
    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }