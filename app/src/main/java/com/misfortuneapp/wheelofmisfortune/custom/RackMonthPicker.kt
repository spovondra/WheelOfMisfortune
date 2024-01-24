package com.misfortuneapp.wheelofmisfortune.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.misfortuneapp.wheelofmisfortune.R
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class MonthPicker(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    private var currentDate: Calendar = Calendar.getInstance()
    private var minDate: Calendar = Calendar.getInstance()
    private var maxDate: Calendar = Calendar.getInstance()

    private val btnPrevious: ImageView
    private val btnNext: ImageView
    private val title: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.month_picker, this, true)

        btnPrevious = findViewById(R.id.btn_previous)
        btnNext = findViewById(R.id.btn_next)
        title = findViewById(R.id.title)

        updateTitle()

        btnPrevious.setOnClickListener {
            val newDate = Calendar.getInstance()
            newDate.time = currentDate.time
            newDate.add(Calendar.MONTH, -1)

            if (!newDate.before(minDate)) {
                currentDate = newDate
                updateTitle()
            }
        }

        btnNext.setOnClickListener {
            if (currentDate.before(maxDate)) {
                currentDate.add(Calendar.MONTH, 1)
                updateTitle()
            }
        }
    }

    fun setDateRange(minDate: String, maxDate: String) {
        this.minDate = parseDate(minDate)
        this.maxDate = parseDate(maxDate)
        // Optionally, you can validate minDate and maxDate to ensure minDate is before maxDate
    }

    private fun parseDate(dateString: String): Calendar {
        val dateFormat = SimpleDateFormat("MM.yyyy", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    private fun updateTitle() {
        val month = DateFormatSymbols().months[currentDate.get(Calendar.MONTH)]
        val year = currentDate.get(Calendar.YEAR)
        title.text = String.format("%s, %d", month, year)
        logSelectedDate()
    }

    private fun logSelectedDate() {
        val selectedDate = SimpleDateFormat("MM.yyyy", Locale.getDefault()).format(currentDate.time)
        // Log the selected date to the console or any other desired output
        println("Selected Date: $selectedDate")
    }
}
