package com.misfortuneapp.wheelofmisfortune.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entita pro reprezentaci času
@Entity
data class TimeRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startTime: Long,
    val endTime: Long
)
