package com.misfortuneapp.wheelofmisfortune.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Reprezentace entitní třídy pro Room databázi
 */
@Entity(tableName = "data_table")
data class DataEntity(
    /**
     * Primární klíč, automaticky generovaný.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * Den, ke kterému jsou data přiřazena.
     */
    val day: Int,

    /**
     * Hodnota dat, představuje získané body (points).
     */
    val value: Double,

    /**
     * Formátované datum (dd.MM) pro daný den.
     */
    val formattedDate: String
)

