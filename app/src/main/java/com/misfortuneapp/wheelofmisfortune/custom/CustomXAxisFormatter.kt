package com.misfortuneapp.wheelofmisfortune.custom

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class CustomXAxisFormatter(private val labels: Array<String>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return if (value >= 0 && value < labels.size) {
            labels[value.toInt()]
        } else {
            ""
        }
    }
}