package com.example.posapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.posapp.R
import com.example.posapp.databinding.ActivityCustomerBinding

class CustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load existing customer data if available
        val existingName = intent.getStringExtra("customerName") ?: ""
        val existingPhone = intent.getStringExtra("customerPhone") ?: ""
        val existingAddress = intent.getStringExtra("customerAddress") ?: ""

        binding.customerNameInput.setText(existingName)
        binding.customerPhoneInput.setText(existingPhone)
        binding.customerAddressInput.setText(existingAddress)

        binding.saveCustomerBtn.setOnClickListener {
            val name = binding.customerNameInput.text.toString().trim()
            val phone = binding.customerPhoneInput.text.toString().trim()
            val address = binding.customerAddressInput.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            } else {
                val resultIntent = Intent()
                resultIntent.putExtra("customerName", name)
                resultIntent.putExtra("customerPhone", phone)
                resultIntent.putExtra("customerAddress", address)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        binding.closeCustomerButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

    }
}