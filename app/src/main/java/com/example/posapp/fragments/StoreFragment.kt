package com.example.posapp.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import com.example.posapp.R
import com.example.posapp.activities.CategoryActivity
import com.example.posapp.activities.ProductActivity
import com.example.posapp.databinding.FragmentStoreBinding

class StoreFragment : Fragment() {

    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStoreBinding.inflate(inflater, container, false)
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
            binding.getUserIdTxt.text = userId.toString()
        }

        binding.productManageBtn.setOnClickListener {
            val intent = Intent(requireContext(), ProductActivity::class.java)
            startActivity(intent)
        }

        binding.categoryManageBtn.setOnClickListener {
            val intent = Intent(requireContext(), CategoryActivity::class.java)
            startActivity(intent)
        }

        binding.switchFingerprint.setOnCheckedChangeListener {_: CompoundButton, isChecked: Boolean ->
            editor.putBoolean("isFingerprintEnabled", isChecked)
            editor.apply()
        }
    }


}