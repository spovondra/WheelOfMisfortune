package com.kolecko.koleckonestesti

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Rozhraní pro presenter (kontroler) statistik
interface StatisticsController {
    suspend fun insertOrUpdateData(date: String, value: Double)
    suspend fun getDataByDate(date: String): DataEntity?
}


// Kontroler pro statistiky, který zpracovává vkládání a aktualizaci dat
class StatisticsControllerImp(private val repository: DataRepository): StatisticsController {

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
}
