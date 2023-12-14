package com.misfortuneapp.wheelofmisfortune.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.misfortuneapp.wheelofmisfortune.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.misfortuneapp.wheelofmisfortune.controller.*
import com.misfortuneapp.wheelofmisfortune.model.*

// Rozhraní pro pohled (view) statistik
interface StatisticsView {
    fun createGraph(series: BarGraphSeries<DataPoint>, formattedDateStrings: Array<String>)
    fun updateStatistics(dailyStatistics: Double, overallStatistics: Double)
}

// Třída pro zobrazování statistik v uživatelském rozhraní
class StatisticsViewImp : AppCompatActivity(), StatisticsView {

    private lateinit var graphView: GraphView
    private lateinit var clearGraphButton: Button
    private lateinit var controller: StatisticsController
    private lateinit var database: DataDatabase
    private lateinit var lastAddedDate: String

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // Nastavení akční lišty
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.statistics_view)
        supportActionBar?.elevation = 0f

        // Inicializace grafu, tlačítka a kontrolleru
        graphView = findViewById(R.id.graph)
        clearGraphButton = findViewById(R.id.clearDataButton)

        // Inicializace databáze a kontroleru s použitím datového rozhraní
        database = DataDatabase.getInstance(this)
        controller = StatisticsControllerImp(DataRepositoryImpl(database.dataDao()), this)
        lastAddedDate = controller.getCurrentDate()

        // Načtení aktuálních dat
        GlobalScope.launch {
            controller.updateGraph()
        }

        // Nastavení posluchače pro tlačítko vymazání grafu
        clearGraphButton.setOnClickListener {
            // Volání metody pro smazání všech dat
            controller.clearAllData()

            // Aktualizace grafu
            GlobalScope.launch {
                controller.updateGraph()
            }
        }
    }

    // Metoda pro vytvoření grafu s danými daty a formátovanými popisky
    override fun createGraph(series: BarGraphSeries<DataPoint>, formattedDateStrings: Array<String>) {
        graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.isYAxisBoundsManual = true

        val maxX = series.highestValueX
        val maxY = series.highestValueY

        graphView.viewport.setMinX(0.5)
        graphView.viewport.setMaxX(maxX + 0.5)

        graphView.viewport.setMinY(0.0)
        graphView.viewport.setMaxY(maxY + 1.0)

        // Odebrání předchozí série a přidání nové série do grafu
        graphView.removeAllSeries()
        graphView.addSeries(series)

        // Zajištění, že jsou alespoň dvě popisky na ose X před jejich nastavením
        if (formattedDateStrings.size >= 2) {
            // Přidání aktuálního data do pole pro popisky na ose X
            graphView.viewport.isXAxisBoundsManual = true
            graphView.viewport.isYAxisBoundsManual = true
            graphView.viewport.setMinX(0.5)
            graphView.viewport.setMinY(0.0)

            // Nastavení popisků
            graphView.gridLabelRenderer.setHorizontalLabelsAngle(35)
            graphView.gridLabelRenderer.labelHorizontalHeight = 60
            graphView.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.NONE

            // Nastavení popisků na ose X pomocí allFormattedDates
            val staticLabelsFormatter = StaticLabelsFormatter(graphView)
            staticLabelsFormatter.setHorizontalLabels(formattedDateStrings)
            graphView.gridLabelRenderer.labelFormatter = staticLabelsFormatter

            val barWidthPx = 25
            series.spacing = barWidthPx
        } else {
            val staticLabelsFormatter = StaticLabelsFormatter(graphView)
            staticLabelsFormatter.setHorizontalLabels(arrayOf("", ""))
            staticLabelsFormatter.setVerticalLabels(arrayOf("", ""))
            graphView.gridLabelRenderer.labelFormatter = staticLabelsFormatter
            graphView.viewport.setMinX(0.0)
            graphView.viewport.setMinY(0.0)
            graphView.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.NONE
        }
    }

    override fun updateStatistics(dailyStatistics: Double, overallStatistics: Double) {
        val dailyStatisticsText = findViewById<TextView>(R.id.dailyStatisticsText)
        val overallStatisticsText = findViewById<TextView>(R.id.overallStatisticsText)

        dailyStatisticsText.text = getString(R.string.daily_statistics, dailyStatistics)
        overallStatisticsText.text = getString(R.string.overall_statistics, overallStatistics)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}