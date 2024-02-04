package com.misfortuneapp.wheelofmisfortune.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
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

        // Přidání MonthPicker do layoutu
        val monthPicker = findViewById<MonthPicker>(R.id.monthPicker)
        configureMonthPicker(monthPicker)
        observeDataForMonthPicker(monthPicker)

        lifecycleScope.launch{
            showAllTasks()
        }
    }

    private fun configureMonthPicker(monthPicker: MonthPicker) {
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

            monthPicker.setDateRange(minDate, maxDate)
        }

        monthPicker.setDateChangeListener { selectedDate ->
            lifecycleScope.launch {
                (controller as StatisticsControllerImp).updateGraph(selectedDate)
            }
        }
    }

    private fun observeDataForMonthPicker(monthPicker: MonthPicker) {
        lifecycleScope.launch {
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

        // Toggle visibility of additional statistics view
        toggleAdditionalStatisticsVisibility(entries.isNotEmpty())
    }

    override fun viewAfterClick(formattedDateStrings: Array<String>) {
        val recyclerView: RecyclerView = findViewById(R.id.statisticsRecyclerView)
        val graphSpace: View = findViewById(R.id.graphSpace)
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
                            mainController,
                        )
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(this@StatisticsViewImp)
                        (recyclerView.layoutManager as LinearLayoutManager).reverseLayout = true
                        graphSpace.visibility = View.VISIBLE
                    }
                }
            }

            override fun onNothingSelected() {
                recyclerView.adapter = null
                graphSpace.visibility = View.GONE
            }
        })
        barChart.invalidate()
    }

    // Method to update statistics in the user interface
    override fun updateStatistics(dailyStatistics: Double, overallStatistics: Double) {
        val dailyStatisticsText = findViewById<TextView>(R.id.dailyStatisticsText)
        val overallStatisticsText = findViewById<TextView>(R.id.overallStatisticsText)

        dailyStatisticsText.text = getString(R.string.daily_statistics, dailyStatistics)
        overallStatisticsText.text = getString(R.string.overall_statistics, overallStatistics)
    }

    private fun toggleAdditionalStatisticsVisibility(hasData: Boolean) {
        val additionalStatisticsView = findViewById<View>(R.id.additionalStatistics)
        val showHideButton: LinearLayout = findViewById(R.id.showHideButton)
        val relativeToShowHide: RelativeLayout = findViewById(R.id.relativeToShowHide)

        if (hasData) {
            additionalStatisticsView.visibility = View.VISIBLE
            showHideButton.visibility = View.VISIBLE
            relativeToShowHide.visibility = View.VISIBLE
        } else {
            additionalStatisticsView.visibility = View.GONE
            showHideButton.visibility = View.GONE
            relativeToShowHide.visibility = View.GONE
        }

        val showHideButtonImg: ImageView = findViewById(R.id.showHideButtonImg)
        relativeToShowHide.visibility = View.GONE

        showHideButton.setOnClickListener {
            if (relativeToShowHide.visibility == View.VISIBLE) {
                relativeToShowHide.visibility = View.GONE
                showHideButtonImg.setImageResource(R.drawable.ic_arrow_right)
            } else {
                relativeToShowHide.visibility = View.VISIBLE
                showHideButtonImg.setImageResource(R.drawable.ic_arrow_down)
            }
        }
    }

    private suspend fun showAllTasks() {
        coroutineScope {
            val allTaskList = findViewById<RecyclerView>(R.id.allTaskList)
            allTaskList.layoutManager = LinearLayoutManager(this@StatisticsViewImp)
            (allTaskList.layoutManager as LinearLayoutManager).reverseLayout = true
            (allTaskList.layoutManager as LinearLayoutManager).stackFromEnd = true

            // Získejte aktuální seznam úkolů přímo z kontroléru s filtrováním podle taskState
            val allTasks = mainController.getAllTasks()

            // Vytvořte nový adaptér s aktuálním seznamem úkolů
            withContext(Dispatchers.Main) {
                // Zobrazení hotových úkolů ve vhodném UI prvku (RecyclerView nebo jiném)
                val adapter = TaskAdapter(
                    allTasks.toMutableList(),
                    { selectedTask -> mainController.openTaskDetailsScreen(selectedTask, this@StatisticsViewImp) },
                    mainController,
                )
                allTaskList.adapter = adapter
            }
        }
    }

    // Method for back navigation in the ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
