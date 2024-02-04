package com.misfortuneapp.wheelofmisfortune.model

import android.content.Context

interface DataRepository {
    suspend fun insertData(dataEntity: DataEntity)
    suspend fun getDataByDate(date: Int): DataEntity?
    suspend fun getAllData(): List<DataEntity>
    suspend fun getAllValues(): List<Double>
    suspend fun getTasksByState(taskState: TaskState): List<Task>
}

class DataRepositoryImpl(context: Context) : DataRepository {
    private val dataDao: DataDao = DataDatabase.getInstance(context).dataDao()
    private val taskDao = TaskDatabase.getDatabase(context).taskDao()

    override suspend fun insertData(dataEntity: DataEntity) {
        dataDao.insertData(dataEntity)
    }

    override suspend fun getDataByDate(date: Int): DataEntity? {
        return dataDao.getDataByDate(date)
    }

    override suspend fun getAllData(): List<DataEntity> {
        return dataDao.getAllData()
    }

    override suspend fun getAllValues(): List<Double> {
        return dataDao.getAllValues()
    }

    override suspend fun getTasksByState(taskState: TaskState): List<Task> {
        return taskDao.getTasksByState(taskState)
    }
}
