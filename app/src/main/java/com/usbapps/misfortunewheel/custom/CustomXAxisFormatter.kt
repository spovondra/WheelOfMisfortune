package com.usbapps.misfortunewheel.custom

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * Vlastní formátovač pro osu X v grafu.
 *
 * @param labels Pole popisků pro osu X.
 */
class CustomXAxisFormatter(private val labels: Array<String>) : ValueFormatter() {
    /**
     * Získá popisek osy pro danou hodnotu.
     *
     * @param value Hodnota na ose X.
     * @param axis Reference na osu.
     * @return Textový popisek pro zobrazení na ose X.
     */
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return if (value >= 0 && value < labels.size) {
            labels[value.toInt()]
        } else {
            ""
        }
    }
}
