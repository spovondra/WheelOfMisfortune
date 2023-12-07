package com.misfortuneapp.wheelofmisfortune.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entita reprezentující časový záznam
@Entity
data class TimeRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,          // Primární klíč s automatickým generováním hodnot
    val startTime: Long,      // Počáteční časový údaj
    val endTime: Long         // Koncový časový údaj
)
