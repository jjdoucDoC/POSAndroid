package com.example.posapp.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.Databases
import com.example.posapp.databinding.ProductSheetItemBinding
import com.example.posapp.models.Products
import com.example.posapp.repository.CategoryRepository
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val context: Context,
    private val cartList: MutableMap<Products, Int>,     // Key(Product) - Value(Int)
    private val onQuantityChange: (Products, Int) -> Unit,   // Callback when the quantity number changes

) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val categoryRepository : CategoryRepository = CategoryRepository.getInstance(context)

    class CartViewHolder(private val binding: ProductSheetItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val productName = binding.productNameOrderSheet
        val priceView = binding.productPriceOrderSheet
        val catName = binding.productCategoryOrderSheet
        val productImage = binding.productOrderSheetImage
        val quantityView = binding.orderSheetProductQuantity
        val decreaseButton = binding.decreaseQuantityBtn
        val increaseButton = binding.increaseQuantityBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ProductSheetItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return CartViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val entry = cartList.entries.toList()[position]
        val item = entry.key
        var quantity = entry.value

        holder.productName.text = item.name
        holder.priceView.text = "${formatCurrency(item.price)}"
        holder.quantityView.text = "${quantity}"

        val bitmap = BitmapFactory.decodeFile(item.imageResId)
        holder.productImage.setImageBitmap(bitmap)

        val category = categoryRepository.getCategoryByID(item.category)
        holder.catName.text = category.name

        // Handle decrease button click
        holder.decreaseButton.setOnClickListener {
            if (quantity > 1) {
                quantity--
                cartList[item] = quantity
                holder.quantityView.text = "${quantity}"
                onQuantityChange(item, quantity)
            } else {
                // Remove product if quantity is 1
                cartList.remove(item)
                notifyItemRemoved(position)
                onQuantityChange(item, 0)
            }
        }

        // Handle increase button click
        holder.increaseButton.setOnClickListener {
            quantity++
            cartList[item] = quantity
            holder.quantityView.text = "${quantity}"
            onQuantityChange(item, quantity)
        }
    }

    // Hàm định dạng giá tiền
    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }
}



