package com.example.posapp.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.posapp.Databases
import com.example.posapp.databinding.ActivityAddCategoryBinding
import com.example.posapp.models.Categories
import com.example.posapp.repository.CategoryRepository

class AddCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var categoryRepository: CategoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryRepository = CategoryRepository.getInstance(this)

        binding.addCatBackStoreBtn.setOnClickListener {
            finish()
        }

        binding.addNewCategoryBtn.setOnClickListener {
            addNewCategory()
        }

    }

    private fun addNewCategory() {
        val categoryName = binding.addCatNameInput.text.toString().trim()

        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Please fill in fields!", Toast.LENGTH_SHORT).show()
            return
        }

        // Kiểm tra trùng tên loại sản phẩm
        val existsCategory = categoryRepository.getCategory().map { it.name.lowercase() }
        if (categoryName.lowercase() in existsCategory) {
            Toast.makeText(this, "Category already exists!", Toast.LENGTH_SHORT).show()
            return
        }

        val category = Categories(
            id = 0,
            name = categoryName)
        val isInserted = categoryRepository.insertCategory(category)
        if (isInserted) {
            Toast.makeText(this, "Category added successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to add category!", Toast.LENGTH_SHORT).show()
        }
    }
}