package com.example.posapp.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.posapp.Databases
import com.example.posapp.R
import com.example.posapp.adapters.CategoryAdapter
import com.example.posapp.adapters.ProductAdapter
import com.example.posapp.databinding.ActivityProductBinding
import com.example.posapp.models.Products
import com.example.posapp.repository.ProductRepository
import java.util.Locale

class ProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var productRepository: ProductRepository
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productRepository = ProductRepository.getInstance(this)
        productAdapter = ProductAdapter(this, productRepository.getProduct())

        setUpSearchProduct()

        binding.productRecycleView.layoutManager = LinearLayoutManager(this)
        binding.productRecycleView.adapter = productAdapter

        binding.productBackStoreBtn.setOnClickListener {
            finish()
        }

        binding.addProductBtn.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }

    }

    private fun setUpSearchProduct() {
        binding.searchProductInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase(Locale.getDefault())
                val filteredList = productRepository.getProduct().filter {
                    it.name.lowercase(Locale.getDefault()).contains(query) ||
                            it.id.toString().contains(query)
                }
                val adapter = ProductAdapter(this@ProductActivity, filteredList)
                binding.productRecycleView.adapter = adapter
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        productAdapter.refreshData(productRepository.getProduct())
    }
}