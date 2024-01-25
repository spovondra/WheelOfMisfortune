package com.misfortuneapp.wheelofmisfortune.controller

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineDataSet
import com.misfortuneapp.wheelofmisfortune.model.DataEntity
import com.misfortuneapp.wheelofmisfortune.model.DataRepository
import com.misfortuneapp.wheelofmisfortune.view.StatisticsView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

interface StatisticsController {
    suspend fun insertOrUpdateData(date: String, currentPoints: Double)
    suspend fun getDataByDate(date: String): DataEntity?
    suspend fun getAllData(): List<DataEntity>
    fun getCurrentDate(): String
    suspend fun updateGraph(selectedDate: String)
    suspend fun getSortedData(selectedDate: String): List<DataEntity>
    suspend fun getFormatedData(selectedDate: String): Array<String>
}

class StatisticsControllerImp(
    private val repository: DataRepository,
    private val view: StatisticsView,
) : StatisticsController {

    private var dailyStatistics: Double = 0.0

    override suspend fun insertOrUpdateData(date: String, currentPoints: Double) {
        val day = date.hashCode()
        val formattedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

        val existingData = repository.getDataByDate(day)

        if (existingData != null) {
            val updatedData = existingData.copy(value = currentPoints, formattedDate = formattedDate)
            repository.insertData(updatedData)
        } else {
            val newData = DataEntity(day = day, value = currentPoints, formattedDate = formattedDate)
            repository.insertData(newData)
        }
    }

    override suspend fun getDataByDate(date: String): DataEntity? {
        val day = date.hashCode()
        return repository.getDataByDate(day)
    }

    private suspend fun calculateAndUpdateOverallStatistics(): Double {
        val allValues = repository.getAllValues()
        return allValues.sum()
    }

    override suspend fun getAllData(): List<DataEntity> {
        return repository.getAllData()
    }

    override fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    override suspend fun getSortedData(selectedDate: String): List<DataEntity> {
        val dataEntities = getAllData().filter {
            val formattedDate = SimpleDateFormat("MM.yyyy", Locale.getDefault()).format(
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(it.formattedDate) ?: Date()
            )
            formattedDate == selectedDate
        }

        return dataEntities.sortedBy { it.formattedDate }
    }

    override suspend fun getFormatedData(selectedDate: String): Array<String> {
        val newFormatedData = getSortedData(selectedDate).map { dataEntity ->
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dataEntity.formattedDate) ?: Date()
            )
        }.toTypedArray()
        return newFormatedData
    }

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
