package com.example.posapp.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.posapp.Databases
import com.example.posapp.R
import com.example.posapp.adapters.BestSellProductAdapter
import com.example.posapp.databinding.FragmentReportBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ReportFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var databases: Databases

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databases = Databases(requireContext())
        pieChart = binding.pieChart
        loadOrderStatistics("today", "To Day")
        showBestSellingProducts("today")

        binding.fillChartBtn.setOnClickListener {
            showDropdownMenu(it)
        }

        binding.pickDayToFillBestSale.setOnClickListener {
            showFilterOptions(it)
        }
    }

    private fun showDropdownMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.chart_time_range_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_today -> loadOrderStatistics("today", "To Day")
                R.id.menu_last_7_days -> loadOrderStatistics("7_days", "Last 7 Days")
                R.id.menu_last_30_days -> loadOrderStatistics("30_days", "Last 30 Days")
            }
            true
        }
        popupMenu.show()
    }

    private fun loadOrderStatistics(timeRange: String, displayText: String) {
        val orderStats = databases.getOrderStatusCount(timeRange)
        val (totalOrders, totalRevenue) = databases.getTotalRevenueAndOrders(timeRange)

        // Lấy số lượng đơn hàng "Shipping" và "Delivered"
        val shippingCount = orderStats["Shipping"] ?: 0
        val deliveredCount = orderStats["Delivered"] ?: 0

        // Cập nhật TextView với icon màu tương ứng
        binding.tvOrderShippingCount.text = "🔵 $shippingCount Shipping"
        binding.tvOrderDeliveredCount.text = "🟢 $deliveredCount Delivered"

        // Cập nhật text cho fillChartBtn
        binding.fillChartBtn.text = displayText

        // Cập nhật tổng số đơn hàng và tổng doanh thu
        binding.totalOrdersText.text = "$totalOrders"
        binding.totalRevenueText.text = formatCurrency(totalRevenue)

        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        orderStats.forEach { (status, count) ->
            entries.add(PieEntry(count.toFloat(), status))
            // Thêm màu tương ứng cho từng trạng thái
            when (status) {
                "Shipping" -> colors.add(Color.BLUE)  // Shipping - Xanh dương
                "Delivered" -> colors.add(Color.GREEN) // Delivered - Xanh lá
            }
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            setDrawValues(false)
        }

        val pieData = PieData(dataSet)
        pieChart.apply {
            data = pieData
            animateY(1000)
            legend.isEnabled = false
            setDrawEntryLabels(false)
            description.isEnabled = false
        }
    }

    private fun showFilterOptions(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.filter_time_range_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_today -> showBestSellingProducts("today")
                R.id.menu_yesterday -> showBestSellingProducts("yesterday")
                R.id.menu_last_7_days -> { showBestSellingProducts("last_7_days") }
                R.id.menu_last_30_days -> { showBestSellingProducts("last_30_days") }
                R.id.menu_this_year -> { showBestSellingProducts("this_year") }
            }
            true
        }
        popupMenu.show()
    }

    private fun showBestSellingProducts(timeRange: String) {
        val bestSellingProducts = databases.getBestSellingProducts(timeRange)
        val adapter = BestSellProductAdapter(requireContext(), bestSellingProducts)
        binding.bestSellProductList.layoutManager = LinearLayoutManager(requireContext())
        binding.bestSellProductList.adapter = adapter
    }

    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}