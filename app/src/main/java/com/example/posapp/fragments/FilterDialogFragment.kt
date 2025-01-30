package com.example.posapp.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.posapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FilterDialogFragment(
    private val onFilterApplied: (startDate: String?, endDate: String?) -> Unit
) : BottomSheetDialogFragment() {

    private var startDate: String? = null
    private var endDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_filter_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        val datePickerLayout = view.findViewById<ConstraintLayout>(R.id.filter_date_picker_layout)
        val closeButton = view.findViewById<ImageView>(R.id.close_filter_dialog_btn)
        val filterButton = view.findViewById<Button>(R.id.filter_btn)
        // Date pickers
        val dateStartButton = view.findViewById<TextView>(R.id.date_start)
        val dateEndButton = view.findViewById<TextView>(R.id.date_end)

        closeButton.setOnClickListener {
            onFilterApplied(null, null)
            dismiss()
        }

        // Date picker dialogs
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Tính toán ngày bắt đầu và ngày kết thúc cho 7 ngày gần đây
        val today = calendar.time
        endDate = dateFormatter.format(today) // Ngày hiện tại
        calendar.add(Calendar.DAY_OF_YEAR, -6) // Lùi lại 6 ngày để có 7 ngày
        startDate = dateFormatter.format(calendar.time)

        datePickerLayout.visibility = View.GONE
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.fill_chooseDay_radio) {
                datePickerLayout.visibility = View.VISIBLE
            } else if (checkedId == R.id.fill_last7days_radio) {
                datePickerLayout.visibility = View.GONE
                // Gán giá trị cho startDate và endDate
                endDate = dateFormatter.format(today)
                calendar.time = today
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                startDate = dateFormatter.format(calendar.time)
            }
        }

        dateStartButton.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    startDate = dateFormatter.format(calendar.time)
                    dateStartButton.text = startDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        dateEndButton.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    endDate = dateFormatter.format(calendar.time)
                    dateEndButton.text = endDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Apply filter
        filterButton.setOnClickListener {
            if (radioGroup.checkedRadioButtonId == R.id.fill_chooseDay_radio && (startDate == null || endDate == null)) {
                Toast.makeText(
                    requireContext(),
                    "Please select both start and end dates",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                onFilterApplied(startDate, endDate)
                dismiss()
            }
        }
    }
}