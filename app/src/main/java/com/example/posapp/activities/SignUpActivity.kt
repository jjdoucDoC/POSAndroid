package com.example.posapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.posapp.Databases
import com.example.posapp.R
import com.example.posapp.models.Users
import com.example.posapp.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var databases: Databases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databases = Databases(this)

        // Back button click
        binding.signUpBackBtn.setOnClickListener {
            finish()
        }

        // Show or Hide Password
        binding.signupShowPassBtn.setOnClickListener {
            if (binding.passSignupInput.inputType == android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                binding.passSignupInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.signupShowPassBtn.setImageResource(R.drawable.visibility_on)
            } else {
                binding.passSignupInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.signupShowPassBtn.setImageResource(R.drawable.visibility_off)
            }
        }

        // Sign Up Account
        binding.signUpBtn.setOnClickListener {
            val email = binding.emailSignupInput.text.toString().trim()
            val phone = binding.phoneSignupInput.text.toString().trim()
            val password = binding.passSignupInput.text.toString().trim()

            // Check Input is empty
            if (email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all information!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if password is less than 8 characters
            if (password.toString().length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check Email or Phone is exsists
            if (databases.checkUserExists(email, phone)) {
                Toast.makeText(this, "Email or phone number already registered!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Add new user
            val newUser = Users(email = email, phone = phone, passWord = password)
            databases.insertUser(newUser)

            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            // Clear all previous activities so it cannot return to the WelcomeActivity screen
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }
}