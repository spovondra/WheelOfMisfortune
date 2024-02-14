package com.usbapps.misfortunewheel.model

import android.content.Context

/**
 * Rozhraní pro repozitář obsahující metody pro práci s daty.
 */
interface DataRepository {
    /**
     * Vloží nová data do repozitáře.
     */
    suspend fun insertData(dataEntity: DataEntity)

    /**
     * Získá data podle zadaného data.
     */
    suspend fun getDataByDate(date: Int): DataEntity?

    /**
     * Získá všechna uložená data.
     */
    suspend fun getAllData(): List<DataEntity>

    /**
     * Získá všechny hodnoty z uložených dat.
     */
    suspend fun getAllValues(): List<Double>

    /**
     * Získá seznam úkolů podle stavu.
     */
    suspend fun getTasksByState(taskState: TaskState): List<Task>
}

/**
 * Implementace rozhraní DataRepository.
 */
class DataRepositoryImpl(context: Context) : DataRepository {
    private val dataDao: DataDao = DataDatabase.getInstance(context).dataDao()
    private val taskDao = TaskDatabase.getDatabase(context).taskDao()

    /**
     * Vloží nová data do repozitáře.
     */
    override suspend fun insertData(dataEntity: DataEntity) {
        dataDao.insertData(dataEntity)
    }

    /**
     * Získá data podle zadaného data.
     */
    override suspend fun getDataByDate(date: Int): DataEntity? {
        return dataDao.getDataByDate(date)
    }

    /**
     * Získá všechna uložená data.
     */
    override suspend fun getAllData(): List<DataEntity> {
        return dataDao.getAllData()
    }

    /**
     * Získá všechny hodnoty z uložených dat.
     */
    override suspend fun getAllValues(): List<Double> {
        return dataDao.getAllValues()
    }

    /**
     * Získá seznam úkolů podle stavu.
     */
    override suspend fun getTasksByState(taskState: TaskState): List<Task> {
        return taskDao.getTasksByState(taskState)
    }
}
