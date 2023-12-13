package com.misfortuneapp.wheelofmisfortune.model

interface DataRepository {
    suspend fun insertData(dataEntity: DataEntity)
    suspend fun getDataByDate(date: Int): DataEntity?
    suspend fun getAllData(): List<DataEntity>
    suspend fun deleteAllData()
    suspend fun getFormattedDates(): Array<String>
    suspend fun getAllValues(): List<Double>
}

class DataRepositoryImpl(private val dataDao: DataDao) : DataRepository {
    override suspend fun insertData(dataEntity: DataEntity) {
        dataDao.insertData(dataEntity)
    }

    override suspend fun getDataByDate(date: Int): DataEntity? {
        return dataDao.getDataByDate(date)
    }

    override suspend fun getAllData(): List<DataEntity> {
        return dataDao.getAllData()
    }

    override suspend fun deleteAllData() {
        return dataDao.deleteAllData()
    }

    override suspend fun getFormattedDates(): Array<String> {
        return dataDao.getFormattedDates()
    }

    override suspend fun getAllValues(): List<Double> {
        return dataDao.getAllValues()
    }
}
