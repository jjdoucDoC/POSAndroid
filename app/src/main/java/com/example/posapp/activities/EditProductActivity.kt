package com.example.posapp.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.posapp.Databases
import com.example.posapp.R
import com.example.posapp.adapters.CategoryMenuAdapter
import com.example.posapp.databinding.ActivityEditProductBinding
import com.example.posapp.models.Products
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class EditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProductBinding
    private lateinit var databases: Databases

    private var productId: Int = -1
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databases = Databases(this)

        // Category Dropdown
        setupCategoryDropdown()

        productId = intent.getIntExtra("product_id", -1)
        if(productId == -1) {
            finish()
            return
        }

        // Get product data to input fields
        val product = databases.getProductByID(productId)
        binding.editProNameInput.setText(product.name)
        binding.editProPriceInput.setText(product.price.toString())

        val categoryId = product.category
        val categoryName = databases.getCategoryByID(categoryId)
        binding.editProCatInput.setText(categoryName.name)

        binding.editProImage.setImageURI(Uri.parse(product.imageResId))
        selectedImageUri = Uri.parse(product.imageResId)

        // Choose New Product Picture
        binding.editChooseImgBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.editProductBtn.setOnClickListener {
            editProduct()
            finish()
        }

        binding.editProBackStoreBtn.setOnClickListener {
            finish()
        }
    }

    private fun editProduct() {
        val newName = binding.editProNameInput.text.toString().trim()
        val newPrice = binding.editProPriceInput.text.toString().trim()
        val newCategory = binding.editProCatInput.text.toString().trim()

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show()
            return
        }

        if (newName.isEmpty() || newPrice.isEmpty() || newCategory.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        // Kiểm tra giá trị của selectedImageUri
        val new_imagePath = if (selectedImageUri.toString() == Uri.parse(databases.getProductByID(productId).imageResId).toString()) {
            // Nếu không thay đổi hình ảnh, giữ lại đường dẫn cũ
            databases.getProductByID(productId).imageResId
        } else {
            // Nếu thay đổi hình ảnh, lưu hình ảnh mới
            saveImageToInternalStorage(selectedImageUri!!) ?: run {
                Toast.makeText(this, "Failed to save image!", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val new_price = newPrice.toIntOrNull()
        if (new_price == null) {
            Toast.makeText(this, "Please enter a valid price!", Toast.LENGTH_SHORT).show()
            return
        }

        val new_categoryId = databases.getCategoryIdByName(newCategory)
        if (new_categoryId == null) {
            Toast.makeText(this, "Invalid category selected!", Toast.LENGTH_SHORT).show()
            return
        }

        val product = Products(
            id = productId,
            name = newName,
            price = new_price,
            imageResId = new_imagePath,
            category = new_categoryId)
        val isUpdated = databases.updateProduct(product)
        if (isUpdated) {
            Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to update product!", Toast.LENGTH_SHORT).show()
        }
    }

    // Lấy danh sách danh mục và hiển thị trong dropdown
    private fun setupCategoryDropdown() {
        val categories = databases.getCategory()

        val adapter = CategoryMenuAdapter(this, categories) { selectedCategory ->
            binding.editProCatInput.setText(selectedCategory, false) // Điền vào ô nhập
            binding.editCategoryMenu.visibility = View.GONE // Ẩn menu dropdown
        }
        binding.editCategoryMenu.layoutManager = LinearLayoutManager(this)
        binding.editCategoryMenu.adapter = adapter

        // Hiển thị dropdown khi nhấn nút
        binding.editProCatInput.setOnClickListener {
            if (binding.editCategoryMenu.visibility == View.GONE) {
                binding.editCategoryMenu.visibility = View.VISIBLE
            } else {
                binding.editCategoryMenu.visibility = View.GONE
            }
        }
    }

    //*** Lưu hình ảnh vào bộ nhớ thiết bị và trả về đường dẫn
    private fun saveImageToInternalStorage(imageUri: Uri): String? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val filename = "image_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, filename)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            file.absolutePath // Trả về đường dẫn của tệp
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            selectedImageUri = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                binding.editProImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to load image!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show()
        }
    }

}