package com.misfortuneapp.wheelofmisfortune.custom.datapicker

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.misfortuneapp.wheelofmisfortune.R
import java.text.DateFormatSymbols
import java.util.*

class MonthAdapter(
    private val context: Context,
    private val onSelectedListener: OnSelectedListener?
) : RecyclerView.Adapter<MonthAdapter.ViewHolder>() {

    private var months: Array<String> = DateFormatSymbols(Locale.ENGLISH).shortMonths
    private var monthType: MonthType = MonthType.TEXT
    private var selectedItem = -1
    private var color: Int = 0
    private var minDate: Calendar? = null
    private var maxDate: Calendar? = null
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    fun setLocale(locale: Locale) {
        months = DateFormatSymbols(locale).shortMonths
        notifyDataSetChanged()
    }
    fun getShortMonth(): String {
        return if (selectedItem in 0 until months.size) {
            if (monthType == MonthType.NUMBER) "${selectedItem + 1}" else months[selectedItem]
        } else {
            // Handle the case when selectedItem is out of bounds
            ""
        }
    }

    fun setSelectedItem(index: Int) {
        if (isWithinMinMaxDate(index, selectedYear)) {
            val previousSelectedItem = selectedItem
            selectedItem = index
            notifyItemChanged(previousSelectedItem)
            notifyItemChanged(selectedItem)
            onSelectedListener?.onContentSelected()
        }
    }

    fun setBackgroundMonth(color: Int) {
        this.color = color
    }

    fun setDateRange(minDate: String?, maxDate: String?) {
        this.minDate = parseDate(minDate)
        this.maxDate = parseDate(maxDate)
        notifyDataSetChanged()
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

    fun setSelectedYear(year: Int) {
        selectedItem = -1
        selectedYear = year
        notifyDataSetChanged()
    }

    fun getMonth(): Int = selectedItem + 1

    private fun isWithinMinMaxDate(position: Int, year: Int): Boolean {
        val cal = Calendar.getInstance()
        cal[Calendar.MONTH] = position
        cal[Calendar.YEAR] = year

        return (minDate == null || cal.timeInMillis >= minDate!!.timeInMillis) &&
                (maxDate == null || cal.timeInMillis <= maxDate!!.timeInMillis)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val layoutMain: LinearLayout = itemView.findViewById(R.id.main_layout)
        val textViewMonth: TextView = itemView.findViewById(R.id.text_month)

        init {
            if (color != 0)
                setMonthBackgroundSelected(color, true)

            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val selectedYear = selectedYear
                if (isWithinMinMaxDate(position, selectedYear)) {
                    setSelectedItem(position)
                    onSelectedListener?.onContentSelected()

                    val selectedMonth = position + 1
                    val formattedDate = "$selectedMonth.$selectedYear"
                }
            }
        }

        private fun setMonthBackgroundSelected(color: Int, isEnabled: Boolean) {
            val layerDrawable =
                ContextCompat.getDrawable(context, R.drawable.month_selected) as LayerDrawable
            val gradientDrawable = layerDrawable.getDrawable(1) as GradientDrawable
            gradientDrawable.setColor(color)
            layerDrawable.setDrawableByLayerId(1, gradientDrawable)

            val states = StateListDrawable()
            states.addState(intArrayOf(android.R.attr.state_selected), gradientDrawable)
            states.addState(intArrayOf(android.R.attr.state_pressed), gradientDrawable)

            val defaultDrawable = if (isEnabled) {
                ContextCompat.getDrawable(context, R.drawable.month_default)
            } else {
                val disabledDrawable =
                    gradientDrawable.constantState!!.newDrawable().mutate() as GradientDrawable
                disabledDrawable.setColor(
                    ContextCompat.getColor(context, android.R.color.darker_gray)
                )
                disabledDrawable
            }
            states.addState(intArrayOf(), defaultDrawable)

            layoutMain.background = states
        }
    }

    interface OnSelectedListener {
        fun onContentSelected()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_view_month, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewMonth.text =
            if (monthType == MonthType.NUMBER) "${position + 1}" else months[position]

        val isWithinDateRange = isWithinMinMaxDate(position, selectedYear)

        holder.textViewMonth.isEnabled = isWithinDateRange
        holder.textViewMonth.alpha = if (isWithinDateRange) 1.0f else 0.5f

        holder.itemView.isClickable = isWithinDateRange

        holder.itemView.isSelected =
            selectedItem == position && isWithinMinMaxDate(selectedItem, selectedYear)

        if (!isWithinDateRange) {
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = months.size
}
