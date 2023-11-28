package com.kolecko.koleckonestesti

import androidx.room.Entity
import androidx.room.PrimaryKey

// Reprezentace entitní třídy pro Room databázi
@Entity(tableName = "data_table")
data class DataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                 // Primární klíč, automaticky generovaný
    val day: Int,                    // Den, ke kterému jsou data přiřazena
    val value: Double,               // Hodnota dat = představuje získané body (points)
    val formattedDate: String        // Formátované datum (dd.MM) pro daný den
)
