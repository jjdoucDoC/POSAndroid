package com.example.posapp.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.posapp.Databases
import com.example.posapp.databinding.ActivityAddCategoryBinding
import com.example.posapp.models.Categories

class AddCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var databases: Databases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databases = Databases(this)

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

        val category = Categories(0, categoryName)
        val isInserted = databases.insertCategory(category)
        if (isInserted) {
            Toast.makeText(this, "Category added successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to add category!", Toast.LENGTH_SHORT).show()
        }
    }
}