package com.kolecko.koleckonestesti

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// Rozhraní pro pohled (view) statistik
interface StatisticsView {
    suspend fun updateGraph()
    fun clearAllData()
}


// Třída pro zobrazování statistik v uživatelském rozhraní
class StatisticsViewImp : AppCompatActivity(), StatisticsView {

    private lateinit var graphView: GraphView
    private lateinit var clearGraphButton: Button
    private lateinit var controller: StatisticsController
    private lateinit var database: DataDatabase
    private var pointCounter: Int = 0
    private lateinit var lastAddedDate: String

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // Nastavení akční lišty
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Statistiky"
        supportActionBar?.elevation = 0f

        // Inicializace grafu, tlačítka a kontrolleru
        graphView = findViewById(R.id.graph)
        clearGraphButton = findViewById(R.id.clearDataButton)

        // Inicializace databáze a kontroleru s použitím datového rozhraní
        database = DataDatabase.getInstance(this)
        controller = StatisticsControllerImp(DataRepository(database.dataDao()))
        lastAddedDate = getCurrentDate()

        // Načtení aktuálních dat
        GlobalScope.launch {
            val currentDate = getCurrentDate()
            val dataEntity = controller.getDataByDate(currentDate)

            // Pokud jsou data pro aktuální den k dispozici, použijte je pro inicializaci čítače bodů
            if (dataEntity != null) {
                pointCounter = dataEntity.value.toInt()
            }

            // Aktualizace grafu
            updateGraph()
        }

        // Nastavení posluchače pro tlačítko vymazání grafu
        clearGraphButton.setOnClickListener {
            // Volání metody pro smazání všech dat
            clearAllData()

            // Aktualizace grafu
            GlobalScope.launch {
                updateGraph()
            }
        }
    }

    // Metoda pro získání aktuálního data ve formátu "yyyy-MM-dd"
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    // Metoda pro vytvoření grafu s danými daty a formátovanými popisky
    private suspend fun createGraph(dataEntities: List<DataEntity>, formattedDateStrings: Array<String>) {
        // Vytvoření bodů pro sloupcový graf
        val dataPoints = dataEntities.mapIndexed { index, dataEntity ->
            DataPoint(index.toDouble() + 1, dataEntity.value)
        }.toTypedArray()

        // Vytvoření série pro sloupcový graf
        val series = BarGraphSeries(dataPoints)

        // Nastavení viditelné oblasti grafu na ose X
        val maxX = dataEntities.size.toDouble()
        graphView.viewport.setMinX(0.5)
        graphView.viewport.setMaxX(maxX + 0.5)
        graphView.viewport.isXAxisBoundsManual = true

        // Nastavení viditelné oblasti grafu na ose Y
        val maxY = dataEntities.maxByOrNull { it.value }?.value ?: 0.0
        graphView.viewport.setMinY(0.0)
        graphView.viewport.setMaxY(maxY)
        graphView.viewport.isYAxisBoundsManual = true

        // Odebrání předchozí série a přidání nové série do grafu
        graphView.removeAllSeries()
        graphView.addSeries(series)

        // Zajištění, že jsou alespoň dvě popisky na ose X před jejich nastavením
        if (formattedDateStrings.size >= 2) {
            // Přidání aktuálního data do pole pro popisky na ose X
            val currentDate = getCurrentDate()
            val dataEntity = controller.getDataByDate(currentDate)
            val formattedCurrentDate = dataEntity?.formattedDate ?: ""
            val allFormattedDates = formattedDateStrings + arrayOf(formattedCurrentDate)

            // Nastavení popisků na ose X pomocí allFormattedDates
            val staticLabelsFormatter = StaticLabelsFormatter(graphView)
            staticLabelsFormatter.setHorizontalLabels(allFormattedDates)
            graphView.gridLabelRenderer.labelFormatter = staticLabelsFormatter
            graphView.gridLabelRenderer.setHorizontalLabelsAngle(35)
            graphView.gridLabelRenderer.labelHorizontalHeight = 70
            graphView.gridLabelRenderer.numHorizontalLabels = allFormattedDates.size
            graphView.gridLabelRenderer.textSize = 30f
        }
    }


    // Metoda pro aktualizaci grafu ve specifickém vlákně
    override suspend fun updateGraph() {
        // Získání všech dat z databáze
        val dataEntities = database.dataDao().getAllData()
        // Získání formátovaných popisků pro osu X z databáze
        val formattedDateStrings = database.dataDao().getFormattedDates()

        // Přepnutí na hlavní vlákno pro aktualizaci UI
        withContext(Dispatchers.Main) {
            // Vytvoření grafu s daty a popisky
            createGraph(dataEntities, formattedDateStrings)
        }
    }

    // Metoda pro smazání všech dat
    @OptIn(DelicateCoroutinesApi::class)
    override fun clearAllData() {
        GlobalScope.launch {
            database.dataDao().deleteAllData()  // Smazání všech dat v databázi
            pointCounter = 0                    // Nastavení čítače bodů na 0
            updateGraph()                       // Aktualizace grafu
        }
    }

    // Přepsání metody pro zpětné tlačítko v akční liště
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
