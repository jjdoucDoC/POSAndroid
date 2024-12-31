package com.example.posapp.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.Databases
import com.example.posapp.databinding.ProductOrderItemBinding
import com.example.posapp.models.Products
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val context: Context,
    private val cartList: MutableList<Products>
//    private val onItemRemoved: (Products) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val databases : Databases = Databases(context)

    class CartViewHolder(private val binding: ProductOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val productName = binding.productNameOrder
        val priceView = binding.productPriceOrder
        val catName = binding.productCategoryOrder
        val productImage = binding.productOrderImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ProductOrderItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return CartViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartList[position]
        holder.productName.text = item.name
        holder.priceView.text = "${formatCurrency(item.price)}"
        val bitmap = BitmapFactory.decodeFile(item.imageResId)
        holder.productImage.setImageBitmap(bitmap)
        val category = databases.getCategoryByID(item.category)
        holder.catName.text = category.name
    }

}

    // Hàm định dạng giá tiền
    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }

