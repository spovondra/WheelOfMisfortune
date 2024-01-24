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

    private var dateChangeListener: ((String) -> Unit)? = null

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
                notifyDateChange()
            }
        }

        btnNext.setOnClickListener {
            if (currentDate.before(maxDate)) {
                currentDate.add(Calendar.MONTH, 1)
                updateTitle()
                notifyDateChange()
            }
        }
    }

    fun setDateRange(minDate: String, maxDate: String) {
        this.minDate = parseDate(minDate)
        this.maxDate = parseDate(maxDate)
    }

    fun getSelectedDateAsString(): String {
        return SimpleDateFormat("MM.yyyy", Locale.getDefault()).format(currentDate.time)
    }

    fun setDateChangeListener(listener: (String) -> Unit) {
        dateChangeListener = listener
    }

    private fun notifyDateChange() {
        dateChangeListener?.invoke(getSelectedDateAsString())
    }

    private fun parseDate(dateString: String): Calendar {
        val dateFormat = SimpleDateFormat("MM.yyyy", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        val calendar = Calendar.getInstance()
        if (date != null) {
            calendar.time = date
        }
        return calendar
    }

    private fun updateTitle() {
        val month = DateFormatSymbols().months[currentDate.get(Calendar.MONTH)]
        val year = currentDate.get(Calendar.YEAR)
        title.text = String.format("%s, %d", month, year)
    }
}
