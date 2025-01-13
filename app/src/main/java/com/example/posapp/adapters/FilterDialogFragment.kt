package com.example.posapp.adapters

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
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
        datePickerLayout.visibility = View.GONE
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.fill_chooseDay_radio) {
                datePickerLayout.visibility = View.VISIBLE
            } else {
                datePickerLayout.visibility = View.GONE
            }
        }

        // Date picker dialogs
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

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