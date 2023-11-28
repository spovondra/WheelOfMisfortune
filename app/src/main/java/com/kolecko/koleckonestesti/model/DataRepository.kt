package com.kolecko.koleckonestesti

// Repozitář pro přístup k datům prostřednictvím DataDao
class DataRepository(private val dataDao: DataDao) {

    // Vložení nových dat do databáze
    suspend fun insertData(dataEntity: DataEntity) {
        dataDao.insertData(dataEntity)
    }

    // Získání dat podle specifického data
    suspend fun getDataByDate(date: Int): DataEntity? {
        return dataDao.getDataByDate(date)
    }

    // Získání všech dat uložených v databázi
    suspend fun getAllData(): List<DataEntity> {
        return dataDao.getAllData()
    }
}
