package com.usbapps.misfortunewheel.controller

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineDataSet
import com.usbapps.misfortunewheel.model.DataEntity
import com.usbapps.misfortunewheel.model.DataRepository
import com.usbapps.misfortunewheel.view.StatisticsView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Rozhraní definující hlavní funkce pro správu statistik.
 */
interface StatisticsController {
    /**
     * Asynchronně vloží nebo aktualizuje údaje o bodech pro zadané datum.
     * @param date Datum ve formátu "dd.MM.yyyy".
     * @param currentPoints Aktuální hodnota bodů.
     */
    suspend fun insertOrUpdateData(date: String, currentPoints: Double)

    /**
     * Asynchronně získá údaje o bodech pro zadané datum.
     * @param date Datum ve formátu "dd.MM.yyyy".
     * @return [DataEntity] nebo null, pokud data neexistují.
     */
    suspend fun getDataByDate(date: String): DataEntity?

    /**
     * Asynchronně získá všechna uložená data.
     * @return Seznam všech [DataEntity].
     */
    suspend fun getAllData(): List<DataEntity>

    /**
     * Získá aktuální datum ve formátu "yyyy-MM-dd".
     * @return Aktuální datum.
     */
    fun getCurrentDate(): String

    /**
     * Asynchronně získá a seřadí údaje o bodech pro zadané datum.
     * @param selectedDate Zvolené datum ve formátu "MM.yyyy".
     * @return Seznam seřazených [DataEntity] pro zadané datum.
     */
    suspend fun getSortedData(selectedDate: String): List<DataEntity>

    /**
     * Asynchronně získá formátovaná data pro graf zadaného datumu.
     * @param selectedDate Zvolené datum ve formátu "MM.yyyy".
     * @return Pole formátovaných dat pro osu x grafu.
     */
    suspend fun getFormatedData(selectedDate: String): Array<String>

    /**
     * Asynchronně aktualizuje graf pro zvolené datum.
     * @param selectedDate Zvolené datum ve formátu "MM.yyyy".
     */
    suspend fun updateGraph(selectedDate: String)
}

/**
 * Implementace rozhraní [StatisticsController].
 */
class StatisticsControllerImp(
    private val repository: DataRepository,
    private val view: StatisticsView,
) : StatisticsController {

    private var dailyStatistics: Double = 0.0

    /**
     * Asynchronně vloží nebo aktualizuje údaje o bodech pro zadané datum.
     */
    override suspend fun insertOrUpdateData(date: String, currentPoints: Double) {
        val day = date.hashCode()
        val formattedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

        val existingData = repository.getDataByDate(day)

        if (existingData != null) {
            val updatedData =
                existingData.copy(value = currentPoints, formattedDate = formattedDate)
            repository.insertData(updatedData)
        } else {
            val newData =
                DataEntity(day = day, value = currentPoints, formattedDate = formattedDate)
            repository.insertData(newData)
        }
    }

    /**
     * Asynchronně získá údaje o bodech pro zadané datum.
     */
    override suspend fun getDataByDate(date: String): DataEntity? {
        val day = date.hashCode()
        return repository.getDataByDate(day)
    }

    /**
     * Asynchronně vypočte a aktualizuje celkové statistiky.
     */
    private suspend fun calculateAndUpdateOverallStatistics(): Double {
        val allValues = repository.getAllValues()
        return allValues.sum()
    }

    /**
     * Asynchronně získá všechna uložená data.
     */
    override suspend fun getAllData(): List<DataEntity> {
        return repository.getAllData()
    }

    /**
     * Získá aktuální datum ve formátu "yyyy-MM-dd".
     */
    override fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    /**
     * Asynchronně získá a seřadí údaje o bodech pro zadané datum.
     */
    override suspend fun getSortedData(selectedDate: String): List<DataEntity> {
        val dataEntities = getAllData().filter {
            val formattedDate = SimpleDateFormat("MM.yyyy", Locale.getDefault()).format(
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(it.formattedDate)
                    ?: Date()
            )
            formattedDate == selectedDate
        }

        return dataEntities.sortedBy { it.formattedDate }
    }

    /**
     * Asynchronně získá formátovaná data pro graf zadaného datumu.
     */
    override suspend fun getFormatedData(selectedDate: String): Array<String> {
        val newFormatedData = getSortedData(selectedDate).map { dataEntity ->
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dataEntity.formattedDate)
                    ?: Date()
            )
        }.toTypedArray()
        return newFormatedData
    }

    /**
     * Asynchronně aktualizuje graf pro zvolené datum.
     */
    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun updateGraph(selectedDate: String) {
        GlobalScope.launch {

            val entries = getSortedData(selectedDate).mapIndexed { index, dataEntity ->
                BarEntry(index.toFloat(), dataEntity.value.toFloat())
            }

            LineDataSet(entries, "Label") // "Label" je název pro legendu

            withContext(Dispatchers.Main) {
                view.createBarChart(entries, getFormatedData(selectedDate))
                view.viewAfterClick(getFormatedData(selectedDate))

                val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                val dataEntity = getDataByDate(currentDate)

                dailyStatistics = dataEntity?.value ?: 0.0

                val overallStatistics = calculateAndUpdateOverallStatistics()

                view.updateStatistics(dailyStatistics, overallStatistics)
            }
        }
    }
}
