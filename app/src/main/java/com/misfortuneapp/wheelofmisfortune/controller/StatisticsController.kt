package com.misfortuneapp.wheelofmisfortune.controller

import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.misfortuneapp.wheelofmisfortune.model.*
import com.misfortuneapp.wheelofmisfortune.view.MainView
import com.misfortuneapp.wheelofmisfortune.view.StatisticsView
import com.misfortuneapp.wheelofmisfortune.view.StatisticsViewImp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Rozhraní pro presenter (kontroler) statistik
interface StatisticsController {
    suspend fun insertOrUpdateData(date: String, value: Double)
    suspend fun getDataByDate(date: String): DataEntity?
    suspend fun calculateAndUpdateOverallStatistics(): Double
    suspend fun getAllData(): List<DataEntity>
    suspend fun deleteAllData()
    suspend fun getFormattedDates(): Array<String>
    fun getCurrentDate(): String
    suspend fun updateGraph()
}

// Kontroler pro statistiky, který zpracovává vkládání a aktualizaci dat
class StatisticsControllerImp(
    private val repository: DataRepository,
    private val view: StatisticsView
): StatisticsController {

    // Metoda pro vložení nebo aktualizaci dat na základě data a hodnoty
    override suspend fun insertOrUpdateData(date: String, value: Double) {
        // Převod data na hash pro použití jako klíč v databázi
        val day = date.hashCode()
        // Formátování data pro zobrazení ve formátu "dd.MM"
        val formattedDate = SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date())

        // Získání existujících dat pro daný den
        val existingData = repository.getDataByDate(day)

        if (existingData != null) {
            // Aktualizace existujících dat, pokud existují
            val updatedData = existingData.copy(value = value, formattedDate = formattedDate)
            repository.insertData(updatedData)
        } else {
            // Vložení nových dat, pokud pro daný den neexistují žádná data
            val newData = DataEntity(day = day, value = value, formattedDate = formattedDate)
            repository.insertData(newData)
        }
    }

    // Metoda pro získání dat pro konkrétní datum
    override suspend fun getDataByDate(date: String): DataEntity? {
        val day = date.hashCode()
        return repository.getDataByDate(day)
    }

    override suspend fun calculateAndUpdateOverallStatistics(): Double {
        val allValues = repository.getAllValues()
        return allValues.sum()
    }

    override suspend fun getAllData(): List<DataEntity> {
        return repository.getAllData()
    }

    override suspend fun deleteAllData() {
        return repository.deleteAllData()
    }

    override suspend fun getFormattedDates(): Array<String> {
        return repository.getFormattedDates()
    }

    override fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun updateGraph() {
        GlobalScope.launch {
            // Získání všech dat z databáze
            val dataEntities = getAllData()
            val series = BarGraphSeries(dataEntities.mapIndexed { index, dataEntity ->
                DataPoint(index.toDouble() + 1, dataEntity.value)
            }.toTypedArray())
            // Získání formátovaných popisků pro osu X z databáze
            val formattedDateStrings = getFormattedDates() + ""

            // Přepnutí na hlavní vlákno pro aktualizaci UI
            withContext(Dispatchers.Main) {
                // Vytvoření grafu s daty a popisky
                view.createGraph(series, formattedDateStrings)

                val dailyStatistics = dataEntities.lastOrNull()?.value ?: 0.0
                val overallStatistics = calculateAndUpdateOverallStatistics()

                // Aktualizace statistiky v UI
                view.updateStatistics(dailyStatistics, overallStatistics)
            }
        }
    }
}
