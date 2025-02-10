package com.example.posapp.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.posapp.Databases
import com.example.posapp.R
import com.example.posapp.databinding.ActivityEditCategoryBinding
import com.example.posapp.models.Categories

class EditCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCategoryBinding
    private lateinit var databases: Databases
    private var categoryId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databases = Databases(this)

        categoryId = intent.getIntExtra("category_id", -1)
        if(categoryId == -1) {
            finish()
            return
        }

        // Get category data to input fields
        val category = databases.getCategoryByID(categoryId)
        binding.editCatNameInput.setText(category.name)

        binding.editCategoryBtn.setOnClickListener {
            editCategory()
            finish()
        }

        binding.editCatBackStoreBtn.setOnClickListener {
            finish()
        }

        binding.deleteCategoryBtn.setOnClickListener {
            showDeleteCategoryDialog(
                "Delete Category",
                "Are you sure you want to delete this category?"
            ) {
                val isDeleted = databases.deleteCategory(categoryId)
                if (isDeleted) {
                    Toast.makeText(this, "Category deleted successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to delete category!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun editCategory() {
        val newName = binding.editCatNameInput.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "Please fill in name fields!", Toast.LENGTH_SHORT).show()
            return
        }

        // Kiểm tra trùng tên loại sản phẩm
        val existsCategory = databases.getCategory().map { it.name.lowercase() }
        if (newName.lowercase() in existsCategory) {
            Toast.makeText(this, "Category already exists!", Toast.LENGTH_SHORT).show()
            return
        }

        val category = Categories(
            id = categoryId,
            name = newName)
        val isUpdated = databases.updateCategory(category)
        if (isUpdated) {
            Toast.makeText(this, "Category updated successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to update category!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteCategoryDialog(
        title: String,
        message: String,
        onDelete: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_delete, null)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialog_message)
        val yesButton = dialogView.findViewById<Button>(R.id.yes_button)
        val noButton = dialogView.findViewById<Button>(R.id.no_button)

        dialogTitle.text = title
        dialogMessage.text = message

        val customDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Handle the Yes button click
        yesButton.setOnClickListener {
            onDelete()
            customDialog.dismiss()  // Close the dialog after action
        }

        // Handle the No button click
        noButton.setOnClickListener {
            customDialog.dismiss()  // Close the dialog if No is clicked
        }

        customDialog.show()  // Show the dialog
    }

    override fun onResume() {
        super.onResume()
    }
}
