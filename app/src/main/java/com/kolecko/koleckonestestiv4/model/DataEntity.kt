package com.kolecko.koleckonestestiv4

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_table")
data class DataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val day: Int, // Nová položka pro den
    val value: Double,
    val formattedDate: String // Přidáno pro ukládání formátovaného data (dd.MM)
)

