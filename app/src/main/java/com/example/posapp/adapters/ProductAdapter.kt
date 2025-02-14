package com.example.posapp.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.Databases
import com.example.posapp.activities.EditProductActivity
import com.example.posapp.R
import com.example.posapp.databinding.ProductItemBinding
import com.example.posapp.models.Products
import com.example.posapp.repository.CategoryRepository
import com.example.posapp.repository.ProductRepository
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val context: Context,
    private var productList: List<Products>
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val categoryRepository : CategoryRepository = CategoryRepository.getInstance(context)
    private val productRepository : ProductRepository = ProductRepository.getInstance(context)

    class ProductViewHolder (private val binding: ProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val nameView = binding.productName
        val priceView = binding.productPrice
        val catName = binding.productCatName
        val showMenuButton = binding.showProDetailMenu
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = productList[position]
        holder.nameView.text = item.name
        holder.priceView.text = formatCurrency(item.price)

        // Fetch category name by category ID
        val category = categoryRepository.getCategoryByID(item.category) // Get category by ID
        holder.catName.text = category.name // Display the category name

        // Show Edit & Delete Product Menu
        holder.showMenuButton.setOnClickListener {
            val popupMenu = PopupMenu(context, holder.showMenuButton)
            popupMenu.menuInflater.inflate(R.menu.ud_product_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {

                    R.id.edit_product -> {
                        val intent = Intent(holder.itemView.context, EditProductActivity::class.java).apply {
                            putExtra("product_id", item.id)
                        }
                        holder.itemView.context.startActivity(intent)
                    }

                    R.id.delete_product -> {
                        productRepository.deleteProduct(item.id)
                        refreshData(productRepository.getProduct())
                        Toast.makeText(holder.itemView.context, "Product deleted!", Toast.LENGTH_SHORT).show()
                    }

                }
                true
            }
            popupMenu.show()
        }
    }

    // Format Currency Function
    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }

    // Refresh Product Data
    fun refreshData(newProducts: List<Products>) {
        productList = newProducts
        notifyDataSetChanged()
    }
}