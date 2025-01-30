package com.example.posapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.posapp.Databases
import com.example.posapp.adapters.OrderAdapter
import com.example.posapp.databinding.FragmentHistoryBinding
import com.example.posapp.models.Orders
import java.text.SimpleDateFormat

class HistoryFragment : Fragment() {

    private lateinit var databases: Databases
    private lateinit var orderList: List<Orders>

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databases = Databases(requireContext())
        orderList = databases.getOrder()

        setupRecyclerView()

        binding.fillOrderDateBtn.setOnClickListener {
            val filterDateDialog = FilterDialogFragment { startDate, endDate ->

                val filteredList = if (startDate != null && endDate != null) {
                    // Lọc danh sách đơn hàng
                    orderList.filter {
                        val orderDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(it.orderDate)
                        val start = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("$startDate 00:00:00")
                        val end = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("$endDate 23:59:59")
                        orderDate != null && orderDate >= start && orderDate <= end
                    }

                } else {
                    orderList
                }

                setupRecyclerView(filteredList)
            }
            filterDateDialog.show(parentFragmentManager, "FilterDialogFragment")
        }


    }

    private fun setupRecyclerView(filteredOrders: List<Orders> = orderList) {
        val orderAdapter = OrderAdapter(requireContext(), filteredOrders)
        binding.orderRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.orderRecyclerview.adapter = orderAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}