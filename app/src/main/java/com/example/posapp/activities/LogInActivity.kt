package com.example.posapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.posapp.Databases
import com.example.posapp.R
import com.example.posapp.databinding.ActivityLogInBinding

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var databases: Databases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databases = Databases(this)

        // Back button click handle
        binding.loginBackBtn.setOnClickListener {
            finish()
        }

        // Show or Hide Password
        binding.loginShowPassBtn.setOnClickListener {
            if (binding.passLoginInput.inputType == android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                // Hiển thị mật khẩu
                binding.passLoginInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.loginShowPassBtn.setImageResource(R.drawable.visibility_on)
            } else {
                binding.passLoginInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.loginShowPassBtn.setImageResource(R.drawable.visibility_off)
            }

            binding.passLoginInput.setSelection(binding.passLoginInput.text.length)
        }

        // Log in
        binding.logInBtn.setOnClickListener {
            val emailOrPhone = binding.emailPhoneLoginInput.text.toString().trim()
            val password = binding.passLoginInput.text.toString().trim()

            // Check Input is empty
            if (emailOrPhone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all information!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check Valid Login
            val userId = databases.isValidUser(emailOrPhone, password)
            if (userId != -1) {
                // Save login status
                val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.putInt("userId", userId)
                editor.apply()

                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                // Xóa tất cả các hoạt động trước đó để không thể quay lại màn hình WelcomeActivity
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Account is invalid!", Toast.LENGTH_SHORT).show()
            }
        }

    }

}