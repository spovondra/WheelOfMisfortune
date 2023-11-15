package com.kolecko.koleckonestestiv4
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
) {
    // Konstruktor pro vytvoření instance Task s určenými vlastnostmi
    constructor(title: String, description: String) : this(0, title, description)
}