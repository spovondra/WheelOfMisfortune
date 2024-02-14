package com.usbapps.misfortunewheel.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entita reprezentující časový záznam v aplikaci.
 *
 * @param id Identifikační číslo časového záznamu v databázi (Primární klíč s automatickým generováním hodnot).
 * @param startTime Počáteční časový údaj časového záznamu.
 * @param endTime Koncový časový údaj časového záznamu.
 */
@Entity
data class TimeRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startTime: Long,
    val endTime: Long
)
