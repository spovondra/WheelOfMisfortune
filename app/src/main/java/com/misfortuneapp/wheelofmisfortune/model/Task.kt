package com.misfortuneapp.wheelofmisfortune.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Enum pro stavy úkolu
enum class TaskState {
    AVAILABLE,
    IN_PROGRESS,
    DONE,
    DELETED
}

// Entita reprezentující úkol v databázi
@Entity
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                                // Identifikační číslo úkolu v databázi
    var title: String,                              // Název úkolu
    var description: String,                        // Popis úkolu
    val points: Int = DEFAULT_POINTS,               // Počet bodů při splnění úkolu, defaultně nastaven na 5
var priority: Int = 0,                              // Priorita úkolu
    var iconResId: Int = 0,                         // ID ikonky úkolu
    var startTime: Long = 0,                        // Čas spuštění úlohy
    var taskState: TaskState = TaskState.AVAILABLE, // Stav úkolu (dostupný, probíhá, hotovo)
    var endTime: Long = 0,                          // Čas do konce úlohy (systémový čas + uživatelem zvolený)
    var completionTime: Long = 0                    // Čas splnění úlohy
) {
    // Alternativní konstruktor pro vytvoření instance Task s určenými vlastnostmi
    constructor(
        title: String,
        description: String,
        priority: Int,
        iconResId: Int,
        startTime: Long,
        taskState: TaskState,
        endTime: Long,
        completionTime: Long
    ) : this(0, title, description, DEFAULT_POINTS, priority, iconResId, startTime, taskState, endTime, completionTime)

    // Společné hodnoty pro všechny instance třídy Task
    companion object {
        const val DEFAULT_POINTS = 1 // Výchozí hodnota pro počet bodů
    }
}