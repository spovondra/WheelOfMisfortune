package com.misfortuneapp.wheelofmisfortune.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Enumerace pro různé stavy úkolu.
 */
enum class TaskState {
    /**
     * Úkol je dostupný ke splnění.
     */
    AVAILABLE,

    /**
     * Úkol je v průběhu vykonávání.
     */
    IN_PROGRESS,

    /**
     * Úkol byl úspěšně dokončen.
     */
    DONE,

    /**
     * Úkol byl smazán.
     */
    DELETED
}

/**
 * Entita reprezentující úkol v databázi.
 */
@Entity
data class Task(
    /**
     * Identifikační číslo úkolu v databázi.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /**
     * Název úkolu.
     */
    var title: String,
    /**
     * Popis úkolu.
     */
    var description: String,
    /**
     * Počet bodů při splnění úkolu.
     */
    val points: Int = DEFAULT_POINTS,
    /**
     * Priorita úkolu.
     */
    var priority: Int = 0,
    /**
     * ID ikonky úkolu.
     */
    var iconResId: Int = 0,
    /**
     * Čas spuštění úlohy.
     */
    var startTime: Long = 0,
    /**
     * Stav úkolu (dostupný, probíhá, hotovo).
     */
    var taskState: TaskState = TaskState.AVAILABLE,
    /**
     * Čas do konce úlohy (systémový čas + uživatelem zvolený).
     */
    var endTime: Long = 0,
    /**
     * Čas splnění úlohy.
     */
    var completionTime: Long = 0
) {
    // Alternativní konstruktor pro vytvoření instance Task s určenými vlastnostmi.
    constructor(
        title: String,
        description: String,
        priority: Int,
        iconResId: Int,
        startTime: Long,
        taskState: TaskState,
        endTime: Long,
        completionTime: Long
    ) : this(
        title = title,
        description = description,
        points = DEFAULT_POINTS,
        priority = priority,
        iconResId = iconResId,
        startTime = startTime,
        taskState = taskState,
        endTime = endTime,
        completionTime = completionTime
    )

    // Společné hodnoty pro všechny instance třídy Task.
    companion object {
        /**
         * Výchozí hodnota pro počet bodů.
         */
        const val DEFAULT_POINTS: Int = 1
    }
}
