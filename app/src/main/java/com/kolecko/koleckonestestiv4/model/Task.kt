package com.kolecko.koleckonestestiv4

data class Task (
    val id: Int,
    val title: String,
    val description: String,
) {
    // Konstruktor pro vytvoření instance Task s určenými vlastnostmi
    constructor(title: String, description: String) : this(0, title, description)
}