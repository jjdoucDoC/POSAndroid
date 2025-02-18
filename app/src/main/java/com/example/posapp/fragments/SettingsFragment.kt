package com.example.posapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import com.example.posapp.R
import com.example.posapp.activities.LogInActivity
import com.example.posapp.databinding.FragmentSettingsBinding
import com.example.posapp.repository.UserRepository

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        userRepository = UserRepository.getInstance(requireContext())
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val lassLoggedInUser = sharedPreferences.getString("lastLoggedInUser", "")
        val currentUser = sharedPreferences.getString("currentLoggedInUser", "")

        // Nếu tài khoản thay đổi, đặt switchFingerprint về false
        if (lassLoggedInUser != currentUser) {
            editor.putBoolean("isFingerprintEnabled", false)
            editor.putString("lastLoggedInUser", currentUser)   // Cập nhật tài khoản gần nhất
            editor.apply()
        }

        // Cập nhật trạng thái Switch từ SharedPreferences
        val isFingerprintEnabled = sharedPreferences.getBoolean("isFingerprintEnabled", false)
        binding.switchFingerprint.isChecked = isFingerprintEnabled

        // Get user ID
        val userId = sharedPreferences.getInt("userId", -1)
        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        } else {
            val getUser = userRepository.getUserByID(userId)
            binding.settingUserEmail.text = getUser.email
            binding.settingUserPhone.text = getUser.phone
        }

        binding.switchFingerprint.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            editor.putBoolean("isFingerprintEnabled", isChecked)
            editor.apply()
        }

        binding.logoutBtn.setOnClickListener {
            editor.putBoolean("isLoggedIn", false)
            editor.apply()

            val intent = Intent(requireContext(), LogInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        }
    }

}