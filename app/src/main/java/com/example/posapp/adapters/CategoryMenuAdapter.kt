package com.example.posapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.databinding.CategoryItemMenuBinding
import com.example.posapp.models.Categories

class CategoryMenuAdapter(
    private val context: Context,
    private var categories: List<Categories>,
    private val onCategorySelected: (String) -> Unit // Callback khi chọn danh mục
) : RecyclerView.Adapter<CategoryMenuAdapter.CategoryViewHolder>() {
    class CategoryViewHolder(private val binding: CategoryItemMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
            val catNameText = binding.categoryNameMenu
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = CategoryItemMenuBinding.inflate(LayoutInflater.from(context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categories[position]
        holder.catNameText.text = item.name

        holder.itemView.setOnClickListener {
            onCategorySelected(item.name)   // Gửi tên loại sản phẩm qua callback
        }
    }
}