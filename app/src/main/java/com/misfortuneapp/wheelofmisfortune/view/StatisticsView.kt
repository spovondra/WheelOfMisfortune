package com.misfortuneapp.wheelofmisfortune.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.misfortuneapp.wheelofmisfortune.R
import com.misfortuneapp.wheelofmisfortune.controller.MainController
import com.misfortuneapp.wheelofmisfortune.controller.MainControllerImpl
import com.misfortuneapp.wheelofmisfortune.controller.StatisticsController
import com.misfortuneapp.wheelofmisfortune.controller.StatisticsControllerImp
import com.misfortuneapp.wheelofmisfortune.custom.CustomXAxisFormatter
import com.misfortuneapp.wheelofmisfortune.custom.MonthPicker
import com.misfortuneapp.wheelofmisfortune.custom.TaskAdapter
import com.misfortuneapp.wheelofmisfortune.model.DataDatabase
import com.misfortuneapp.wheelofmisfortune.model.DataRepositoryImpl
import com.misfortuneapp.wheelofmisfortune.model.TaskModel
import com.misfortuneapp.wheelofmisfortune.model.TaskModelImpl
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


interface StatisticsView {
    fun createBarChart(entries: List<BarEntry>, formattedDateStrings: Array<String>)
    fun updateStatistics(dailyStatistics: Double, overallStatistics: Double)
    fun viewAfterClick(formattedDateStrings: Array<String>)
}

class StatisticsViewImp : AppCompatActivity(), StatisticsView {
    private lateinit var barChart: BarChart
    private lateinit var controller: StatisticsController
    private lateinit var database: DataDatabase
    private lateinit var mainController: MainController
    private lateinit var mainView: MainView

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

        // Initialize database and controller using data interface
        database = DataDatabase.getInstance(this)
        val taskRepository: TaskModel = TaskModelImpl(this)
        controller = StatisticsControllerImp(DataRepositoryImpl(this), this)
        mainView = MainViewImp()
        mainController = MainControllerImpl(mainView as MainViewImp, mainView, taskRepository, controller)

        swipeToDeleteButton ()

        // Přidání MonthPicker do layoutu
        val monthPicker = findViewById<MonthPicker>(R.id.monthPicker)

        lifecycleScope.launch {
            // Zadání rozsahu měsíců a let (MM.YYYY až MM.YYYY)
            val allData = controller.getAllData()

            val minDate = if (allData.isNotEmpty()) {
                allData.map { it.formattedDate }
                    .minByOrNull { it.substring(3) }?.substring(3) ?: ""
            } else {
                // Nastavení aktuálního dne ve formátu MM.rrrr, pokud getAllData() vrací null nebo je prázdný
                SimpleDateFormat("MM.yyyy", Locale.getDefault()).format(Date())
            }

            val maxDate = if (allData.isNotEmpty()) {
                allData.map { it.formattedDate }
                    .maxByOrNull { it.substring(3) }?.substring(3) ?: ""
            } else {
                // Nastavení aktuálního dne ve formátu MM.rrrr, pokud getAllData() vrací null nebo je prázdný
                SimpleDateFormat("MM.yyyy", Locale.getDefault()).format(Date())
            }

            Log.d("YourTag", "Min Date: $minDate")
            Log.d("YourTag", "Max Date: $maxDate")


            monthPicker.setDateRange(minDate, maxDate)
        }

        // Set the listener to update the graph when the date changes
        monthPicker.setDateChangeListener { selectedDate ->
            GlobalScope.launch {
                (controller as StatisticsControllerImp).updateGraph(selectedDate)
            }
        }

        GlobalScope.launch {
            controller.updateGraph(monthPicker.getSelectedDateAsString())
        }
    }

    // Method to create BarChart with given data and formatted labels
    override fun createBarChart(entries: List<BarEntry>, formattedDateStrings: Array<String>) {
        val dataSet = BarDataSet(entries, "Počet splěných úloh")

        val textColorPrimary = ContextCompat.getColor(this, R.color.inverted)

        dataSet.color = ContextCompat.getColor(this, R.color.iconInactiveColor)
        dataSet.highLightColor = ContextCompat.getColor(this, R.color.iconColor)
        dataSet.valueTextColor = textColorPrimary

        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getBarLabel(barEntry: BarEntry?): String {
                return barEntry?.y?.toInt().toString()
            }
        }

        val barData = BarData(dataSet)
        barChart.data = barData

        val xAxis = barChart.xAxis
        val modifiedDateStrings = formattedDateStrings.map { it.dropLast(5) }.toTypedArray()
        xAxis.valueFormatter = CustomXAxisFormatter(modifiedDateStrings)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = textColorPrimary
        xAxis.axisMinimum = -0.5f
        xAxis.axisMaximum = formattedDateStrings.size.toFloat() - 0.5f
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false) // Odebrat mřížku

        val leftYAxis = barChart.axisLeft
        leftYAxis.textColor = textColorPrimary
        leftYAxis.axisMinimum = 0f
        leftYAxis.labelCount = entries.size + 1
        leftYAxis.granularity = 1f
        leftYAxis.setDrawGridLines(false) // Remove grid lines

        val xAxisData = formattedDateStrings.mapIndexed { index, _ ->
            BarEntry(index.toFloat(), 0f)
        }

        val xAxisDataSet = BarDataSet(xAxisData, null) // Null removes the description label
        xAxisDataSet.color = textColorPrimary
        xAxisDataSet.valueTextColor = textColorPrimary

        // Odebrání prvků z grafu
        barChart.isHighlightPerDragEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.isDoubleTapToZoomEnabled = false
        barChart.isScaleYEnabled = false
        barChart.isScaleXEnabled = false
        barChart.legend.isEnabled = false

        barChart.invalidate()
    }

    override fun viewAfterClick(formattedDateStrings: Array<String>) {
        val recyclerView: RecyclerView = findViewById(R.id.statisticsRecyclerView)
        recyclerView.adapter = null
        barChart.highlightValues(null)

        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                // Získání vybrané hodnoty z grafu (datum)
                val selectedDateIndex = e?.x?.toInt() ?: return
                val selectedDate = formattedDateStrings.getOrNull(selectedDateIndex) ?: return

                lifecycleScope.launch {
                    // Získání hotových úkolů pro vybraný den
                    val doneTasksForSelectedDate = mainController.getDoneTasksForDate(selectedDate)

                    withContext(Dispatchers.Main) {
                        // Zobrazení hotových úkolů ve vhodném UI prvku (RecyclerView nebo jiném)
                        val adapter = TaskAdapter(
                            doneTasksForSelectedDate.toMutableList(),
                            { selectedTask -> mainController.openTaskDetailsScreen(selectedTask, this@StatisticsViewImp) },
                            { removedTask ->
                                lifecycleScope.launch {
                                    (mainController as MainControllerImpl).removeTask(removedTask, false)
                                }
                            },
                            mainController,
                            false
                        )
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(this@StatisticsViewImp)
                        (recyclerView.layoutManager as LinearLayoutManager).reverseLayout = true
                    }
                }
            }

            override fun onNothingSelected() {
                recyclerView.adapter = null
            }
        })
        barChart.invalidate()
    }

    private fun swipeToDeleteButton() {
        val statisticsList = findViewById<RecyclerView>(R.id.statisticsRecyclerView)
        mainController.swipeHelperToDeleteAndEdit(statisticsList,false, this@StatisticsViewImp)
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
