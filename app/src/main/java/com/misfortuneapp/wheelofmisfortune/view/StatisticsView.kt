package com.misfortuneapp.wheelofmisfortune.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.misfortuneapp.wheelofmisfortune.R
import com.misfortuneapp.wheelofmisfortune.controller.StatisticsController
import com.misfortuneapp.wheelofmisfortune.controller.StatisticsControllerImp
import com.misfortuneapp.wheelofmisfortune.custom.CustomXAxisFormatter
import com.misfortuneapp.wheelofmisfortune.model.DataDatabase
import com.misfortuneapp.wheelofmisfortune.model.DataRepositoryImpl
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface StatisticsView {
    fun createBarChart(entries: List<BarEntry>, formattedDateStrings: Array<String>)
    fun updateStatistics(dailyStatistics: Double, overallStatistics: Double)
}

class StatisticsViewImp : AppCompatActivity(), StatisticsView {

    private lateinit var barChart: BarChart
    private lateinit var clearGraphButton: Button
    private lateinit var controller: StatisticsController
    private lateinit var database: DataDatabase

    @SuppressLint("MissingInflatedId")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // ActionBar settings
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.statistics_view)
        supportActionBar?.elevation = 0f

        // Initialize BarChart, button, and controller
        barChart = findViewById(R.id.barChart)
        clearGraphButton = findViewById(R.id.clearDataButton)

        // Initialize database and controller using data interface
        database = DataDatabase.getInstance(this)
        controller = StatisticsControllerImp(DataRepositoryImpl(database.dataDao()), this)

        // Load current data
        GlobalScope.launch {
            controller.updateGraph()
        }

        // Set listener for the clear graph button
        clearGraphButton.setOnClickListener {
            // Call method to clear all data
            controller.clearAllData()

            // Update the graph
            GlobalScope.launch {
                controller.updateGraph()
            }
        }
    }

    // Method to create BarChart with given data and formatted labels
    override fun createBarChart(entries: List<BarEntry>, formattedDateStrings: Array<String>) {
        val dataSet = BarDataSet(entries, "Počet splěných úloh")

        val textColorPrimary = ContextCompat.getColor(this, R.color.inverted)

        dataSet.color = textColorPrimary
        dataSet.valueTextColor = textColorPrimary

        val barData = BarData(dataSet)
        barChart.data = barData

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = CustomXAxisFormatter(formattedDateStrings)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.textColor = textColorPrimary
        xAxis.setDrawGridLines(false) // Odebrat mřížku
        xAxis.textColor = textColorPrimary // Přidat nastavení barvy popisků

        val leftYAxis = barChart.axisLeft
        leftYAxis.textColor = textColorPrimary
        leftYAxis.axisMinimum = 0f
        leftYAxis.labelCount = entries.size + 1
        leftYAxis.granularity = 1f
        leftYAxis.setDrawGridLines(false) // Odebrat mřížku

        barChart.axisRight.isEnabled = false

        val xAxisData = formattedDateStrings.mapIndexed { index, _ ->
            BarEntry(index.toFloat(), 0f)
        }

        val xAxisDataSet = BarDataSet(xAxisData, null) // Null removes the description label
        xAxisDataSet.color = textColorPrimary
        xAxisDataSet.valueTextColor = textColorPrimary

        barChart.xAxis.axisMinimum = -0.5f
        barChart.xAxis.axisMaximum = formattedDateStrings.size.toFloat() - 0.5f
        barChart.xAxis.isGranularityEnabled = true
        barChart.xAxis.granularity = 1f

        // Odebrat popisek v pravém dolním rohu
        barChart.description.isEnabled = false

        barChart.invalidate()
    }

    // Method to update statistics in the user interface
    override fun updateStatistics(dailyStatistics: Double, overallStatistics: Double) {
        val dailyStatisticsText = findViewById<TextView>(R.id.dailyStatisticsText)
        val overallStatisticsText = findViewById<TextView>(R.id.overallStatisticsText)

        dailyStatisticsText.text = getString(R.string.daily_statistics, dailyStatistics)
        overallStatisticsText.text = getString(R.string.overall_statistics, overallStatistics)
    }

    // Method for back navigation in the ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
