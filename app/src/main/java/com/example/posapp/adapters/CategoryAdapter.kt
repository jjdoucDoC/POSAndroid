package com.example.posapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.Databases
import com.example.posapp.activities.EditCategoryActivity
import com.example.posapp.databinding.CategoryItemBinding
import com.example.posapp.models.Categories
import com.example.posapp.models.Products

class CategoryAdapter(
    private val context: Context,
    private var categoryList: List<Categories>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(private val binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root){
        val nameView = binding.categoryName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categoryList[position]
        holder.nameView.text = item.name

        holder.nameView.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditCategoryActivity::class.java).apply {
                putExtra("category_id", item.id)
                putExtra("name", item.name)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    // Làm mới danh sách loại sản phẩm
    fun refreshData(newCategory: List<Categories>) {
        categoryList = newCategory
        notifyDataSetChanged()
    }
}