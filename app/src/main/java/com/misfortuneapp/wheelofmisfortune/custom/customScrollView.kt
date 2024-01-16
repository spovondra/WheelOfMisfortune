package com.misfortuneapp.wheelofmisfortune.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

// Vlastní implementace ScrollView umožňující okamžité posunutí na zadanou pozici.
class CustomScrollView : ScrollView {

    // Konstruktory pro vytvoření CustomScrollView.
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    // Metoda pro posunutí obsahu na zadané souřadnice.
    fun scrollToChild(linesToScroll: Int) {
        // Okamžité posunutí na zadané souřadnice
        scrollTo(0, linesToScroll)
    }
}
