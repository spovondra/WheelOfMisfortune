package com.kolecko.koleckonestesti.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entita reprezentující úkol v databázi
@Entity
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,             // Identifikační číslo úkolu
    val title: String,            // Název úkolu
    val description: String,      // Popis úkolu
    val points: Int = DEFAULT_POINTS, // Počet bodů při splnění úkolu, defaultně nastaven na 5
    val priority: Int = 0,        // Priorita úkolu
    val iconResId: Int = 0        // ID ikonky úkolu
) {
    // Alternativní konstruktor pro vytvoření instance Task s určenými vlastnostmi
    constructor(
        title: String,
        description: String,
        priority: Int,
        iconResId: Int
    ) : this(0, title, description, DEFAULT_POINTS, priority, iconResId)

    // Společné hodnoty pro všechny instance třídy Task
    companion object {
        const val DEFAULT_POINTS = 5 // Výchozí hodnota pro počet bodů
    }
}
