package com.example.posapp.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.posapp.Databases
import com.example.posapp.adapters.CategoryMenuAdapter
import com.example.posapp.databinding.ActivityAddProductBinding
import com.example.posapp.models.Products
import com.example.posapp.repository.CategoryRepository
import com.example.posapp.repository.ProductRepository
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var productRepository: ProductRepository
    private lateinit var categoryRepository: CategoryRepository
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productRepository = ProductRepository.getInstance(this)
        categoryRepository = CategoryRepository.getInstance(this)

        // Category Dropdown
        setupCategoryDropdown()

        // Back Button
        binding.addProBackStoreBtn.setOnClickListener {
            finish()
        }

        // Choose Photo Button
        binding.addChooseImgBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.addNewProductBtn.setOnClickListener {
            addProduct()
        }
    }

    private fun addProduct() {
        val productName = binding.addProNameInput.text.toString().trim()
        val productPrice = binding.addProPriceInput.text.toString().trim()
        val productCategory = binding.addProCatInput.text.toString().trim()

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show()
            return
        }

        if (productName.isEmpty() || productPrice.isEmpty() || productCategory.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val imagePath = saveImageToInternalStorage(selectedImageUri!!)
        if (imagePath == null) {
            Toast.makeText(this, "Failed to save image!", Toast.LENGTH_SHORT).show()
            return
        }

        val price = productPrice.toIntOrNull()
        if (price == null) {
            Toast.makeText(this, "Please enter a valid price!", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryId = categoryRepository.getCategoryIdByName(productCategory)
        if (categoryId == null) {
            Toast.makeText(this, "Invalid category selected!", Toast.LENGTH_SHORT).show()
            return
        }

        val product = Products(
            id = 0,
            name = productName,
            price = price,
            imageResId = imagePath,
            category = categoryId)
        val isInserted = productRepository.insertProduct(product)
        if (isInserted) {
            Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to add product!", Toast.LENGTH_SHORT).show()
        }
    }

    // Lấy danh sách danh mục và hiển thị trong dropdown
    private fun setupCategoryDropdown() {
        val categories = categoryRepository.getCategory()

        val adapter = CategoryMenuAdapter(this, categories) { selectedCategory ->
            binding.addProCatInput.setText(selectedCategory, false) // Điền vào ô nhập
            binding.categoryDropdownMenu.visibility = View.GONE // Ẩn menu dropdown
        }
        binding.categoryDropdownMenu.layoutManager = LinearLayoutManager(this)
        binding.categoryDropdownMenu.adapter = adapter

        // Hiển thị dropdown khi nhấn nút
        binding.addProCatInput.setOnClickListener {
            if (binding.categoryDropdownMenu.visibility == View.GONE) {
                binding.categoryDropdownMenu.visibility = View.VISIBLE
            } else {
                binding.categoryDropdownMenu.visibility = View.GONE
            }
        }
    }

    // Lưu hình ảnh vào bộ nhớ thiết bị và trả về đường dẫn
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                binding.addProImage.setImageBitmap(bitmap) // Hiển thị ảnh trong ImageView
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to load image!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}