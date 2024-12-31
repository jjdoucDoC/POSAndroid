package com.example.posapp.activities

import android.content.Intent
import android.os.Bundle
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

class ProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var databases: Databases
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databases = Databases(this)
        productAdapter = ProductAdapter(this, databases.getProduct())

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

    override fun onResume() {
        super.onResume()
        productAdapter.refreshData(databases.getProduct())
    }
}