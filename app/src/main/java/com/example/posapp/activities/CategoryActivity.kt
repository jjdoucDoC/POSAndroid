package com.example.posapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.posapp.Databases
import com.example.posapp.R
import com.example.posapp.adapters.CategoryAdapter
import com.example.posapp.databinding.ActivityCategoryBinding

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var databases: Databases
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databases = Databases(this)
        categoryAdapter = CategoryAdapter(this, databases.getCategory())

        binding.categoryRecycleView.layoutManager = LinearLayoutManager(this)
        binding.categoryRecycleView.adapter = categoryAdapter

        binding.categoryBackStoreBtn.setOnClickListener {
            finish()
        }

        binding.addCategoryBtn.setOnClickListener {
            val intent = Intent(this, AddCategoryActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        categoryAdapter.refreshData(databases.getCategory())
    }
}