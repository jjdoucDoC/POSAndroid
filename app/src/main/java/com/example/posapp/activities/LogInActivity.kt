package com.example.posapp.activities

import android.content.Context
import android.content.Intent
import androidx.biometric.BiometricPrompt
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.posapp.R
import com.example.posapp.databinding.ActivityLogInBinding
import com.example.posapp.repository.UserRepository
import java.util.concurrent.Executor

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var userRepository: UserRepository
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var timeoutHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository.getInstance(this)

        // Kiểm tra nếu tính năng đăng nhập bằng vân tay đã được bật
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isFingerprintEnabled = sharedPreferences.getBoolean("isFingerprintEnabled", false)
        val lastLoggedInUser = sharedPreferences.getString("lastLoggedInUser", "")

        // Hiển thị tài khoản đăng nhập gần nhất
        if (!lastLoggedInUser.isNullOrEmpty()) {
            binding.emailPhoneLoginInput.setText(lastLoggedInUser)
            binding.emailPhoneLoginInput.isEnabled = false
        }

        // Kiểm tra nếu thiết bị có hỗ trợ vân tay
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            binding.allowFingerprintBtn.isEnabled = false
            Toast.makeText(this, "Thiết bị không hỗ trợ vân tay", Toast.LENGTH_LONG).show()
        } else if (isFingerprintEnabled) {
            setupBiometricAuthentication()
            binding.allowFingerprintBtn.visibility = View.VISIBLE
        }

        binding.allowFingerprintBtn.setOnClickListener {
            setupBiometricAuthentication()
        }

        binding.changeAccountBtn.setOnClickListener {
            binding.emailPhoneLoginInput.isEnabled = true
            binding.allowFingerprintBtn.visibility = View.GONE
            binding.emailPhoneLoginInput.setText("")

            // Xóa tài khoản đăng nhập gần nhất
            val editor = sharedPreferences.edit()
            editor.putString("lastLoggedInUser", "")
            editor.putBoolean("isFingerprintEnabled", false)
            editor.apply()
        }

        // Back button click handle
        binding.loginBackBtn.setOnClickListener {
            finish()
        }

        // Show or Hide Password
        binding.loginShowPassBtn.setOnClickListener {
            if (binding.passLoginInput.inputType == android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            ) {
                // Hiển thị mật khẩu
                binding.passLoginInput.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.loginShowPassBtn.setImageResource(R.drawable.visibility_on)
            } else {
                binding.passLoginInput.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
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
            val userId = userRepository.isValidUser(emailOrPhone, password)
            if (userId != -1) {
                // Save login status
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.putInt("userId", userId)
                editor.putString("currentLoggedInUser", emailOrPhone) // Lưu tài khoản hiện tại
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

    // Cấu hình xác thực vân tay
    private fun setupBiometricAuthentication() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Thiết bị hỗ trợ vân tay, tiến hành xác thực
                val executor: Executor = ContextCompat.getMainExecutor(this)
                biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            timeoutHandler.removeCallbacksAndMessages(null)  // Hủy timeout
                            Toast.makeText(applicationContext, "Xác thực thành công!", Toast.LENGTH_SHORT).show()
                            navigateToMainActivity()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            timeoutHandler.removeCallbacksAndMessages(null)  // Hủy timeout
                            Toast.makeText(applicationContext, "Lỗi xác thực: $errString", Toast.LENGTH_SHORT).show()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Toast.makeText(
                                applicationContext,
                                "Xác thực thất bại",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })

                promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Đăng nhập bằng vân tay")
                    .setSubtitle("Sử dụng Vân tay để mở khóa PosApp")
                    .setNegativeButtonText("Hủy")
                    .build()

                biometricPrompt.authenticate(promptInfo)

                // Đặt thời gian tự động đóng cửa sổ vân tay sau 10 giây nếu không có phản hồi
               timeoutHandler = Handler(Looper.getMainLooper())
                timeoutHandler.postDelayed({
                    biometricPrompt.cancelAuthentication()
                }, 10_000)
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Thiết bị không hỗ trợ hoặc chưa cài đặt vân tay
                Toast.makeText(this, "Thiết bị không hỗ trợ vân tay", Toast.LENGTH_SHORT).show()
                binding.allowFingerprintBtn.isEnabled = false
            }
        }

    }

    // Chuyển hướng đến màn hình chính
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}