package com.kolecko.koleckonestestiv4

class DataRepository(private val dataDao: DataDao) {

    suspend fun insertData(dataEntity: DataEntity) {
        dataDao.insertData(dataEntity)
    }

    suspend fun getDataByDate(date: Int): DataEntity? {
        return dataDao.getDataByDate(date)
    }

    suspend fun getAllData(): List<DataEntity> {
        return dataDao.getAllData()
    }
}
