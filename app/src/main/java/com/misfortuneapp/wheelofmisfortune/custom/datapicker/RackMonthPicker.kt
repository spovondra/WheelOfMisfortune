package com.misfortuneapp.wheelofmisfortune.custom.datapicker

import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.misfortuneapp.wheelofmisfortune.R
import java.util.*
import kotlin.math.max
import kotlin.math.min

class RackMonthPicker(private val context: Context) {

    // Make contentView public
    var contentView: View
        private set

    private val builder = Builder()

    init {
        // Access contentView from the builder
        contentView = builder.contentView
    }

    fun show() {
        if (builder.isBuild) builder.setDefaultValues()
        else {
            builder.build()
            builder.isBuild = true
        }
    }

    fun setLocale(locale: Locale): RackMonthPicker {
        builder.setLocale(locale)
        return this
    }

    fun setDateRange(minDate: String?, maxDate: String?): RackMonthPicker {
        builder.setDateRange(minDate, maxDate)
        return this
    }

    private inner class Builder : MonthAdapter.OnSelectedListener {

        val monthAdapter = MonthAdapter(context, this)
        var mTitleView: TextView? = null
        var mYear: TextView? = null
        var year: Int = 0
        var month: Int = 0

        var contentView: View =
            LayoutInflater.from(context).inflate(R.layout.dialog_month_picker, null)
        var isBuild = false

        var minDate: Calendar? = null
        var maxDate: Calendar? = null

        init {
            mTitleView = contentView.findViewById(R.id.title)
            mYear = contentView.findViewById(R.id.text_year)

            contentView.findViewById<ImageView>(R.id.btn_next).setOnClickListener { nextButtonClick() }
            contentView.findViewById<ImageView>(R.id.btn_previous).setOnClickListener { previousButtonClick() }

            val recyclerView: RecyclerView = contentView.findViewById(R.id.recycler_view)
            recyclerView.layoutManager = GridLayoutManager(context, 4)
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = monthAdapter

            setColorTheme(
                getColorByThemeAttr(
                    context,
                    android.R.attr.colorPrimary,
                    R.color.colorPrimary
                )
            )

            setDefaultValues()
        }

        private fun getColorByThemeAttr(context: Context, attr: Int, defaultColor: Int): Int {
            val typedValue = TypedValue()
            val theme = context.theme
            val got = theme.resolveAttribute(attr, typedValue, true)
            return if (got) typedValue.data else defaultColor
        }

        fun setDefaultValues() {
            val cal = Calendar.getInstance()
            year = cal[Calendar.YEAR]
            month = cal[Calendar.MONTH]

            if (!isWithinDateRange(month, year)) {
                setYearAndMonthWithinRange()
            }

            monthAdapter.setSelectedItem(month)
            monthAdapter.setSelectedYear(year) // Add this line to set the initial selected year
            updateUI()
        }

        private fun setYearAndMonthWithinRange() {
            val minCal = minDate ?: Calendar.getInstance()
            val maxCal = maxDate ?: Calendar.getInstance()

            year = max(minCal[Calendar.YEAR], min(maxCal[Calendar.YEAR], year))
            month = max(minCal[Calendar.MONTH], min(maxCal[Calendar.MONTH], month))
        }

        private fun isWithinDateRange(position: Int, year: Int): Boolean {
            val cal = Calendar.getInstance()
            cal[Calendar.MONTH] = position
            cal[Calendar.YEAR] = year

            return (minDate == null || cal.timeInMillis >= minDate!!.timeInMillis) &&
                    (maxDate == null || cal.timeInMillis <= maxDate!!.timeInMillis)
        }

        fun setDateRange(minDate: String?, maxDate: String?) {
            this.minDate = parseDate(minDate)
            this.maxDate = parseDate(maxDate)
            monthAdapter.setDateRange(minDate, maxDate)
        }

        private fun parseDate(dateString: String?): Calendar? {
            return try {
                val cal = Calendar.getInstance()
                val parts = dateString?.split(".")
                cal[Calendar.MONTH] = parts?.getOrNull(0)?.toIntOrNull()?.minus(1) ?: 0
                cal[Calendar.YEAR] = parts?.getOrNull(1)?.toIntOrNull() ?: 0
                cal
            } catch (e: Exception) {
                null
            }
        }

        fun setLocale(locale: Locale) {
            monthAdapter.setLocale(locale)
        }

        fun setColorTheme(color: Int) {
            contentView.findViewById<LinearLayout>(R.id.linear_toolbar).setBackgroundColor(color)
            monthAdapter.setBackgroundMonth(color)
        }

        fun build() {
            monthAdapter.setSelectedItem(month)
            updateUI()
            (context as? AppCompatActivity)?.setContentView(contentView)
        }

        private fun nextButtonClick() {
            val nextYear = year + 1

            if (nextYear <= maxDate?.get(Calendar.YEAR) ?: Int.MAX_VALUE) {
                year = nextYear
            }

            mYear?.text = year.toString()
            monthAdapter.setSelectedYear(year)
            updateUIWithTransition()
        }

        private fun previousButtonClick() {
            val previousYear = year - 1

            if (previousYear >= minDate?.get(Calendar.YEAR) ?: Int.MIN_VALUE) {
                year = previousYear
            }

            mYear?.text = year.toString()
            monthAdapter.setSelectedYear(year)
            updateUIWithTransition()
        }

        private fun updateUI() {
            mTitleView?.text = "${monthAdapter.getShortMonth()}, $year"
            mYear?.text = year.toString()
        }

        private fun updateUIWithTransition() {
            TransitionManager.beginDelayedTransition(
                contentView.findViewById(R.id.linear_toolbar),
                AutoTransition()
            )
            updateUI()
        }

        override fun onContentSelected() {
            updateUI()
        }
    }
}
