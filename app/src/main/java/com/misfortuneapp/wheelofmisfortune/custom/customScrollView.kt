package com.misfortuneapp.wheelofmisfortune.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

class CustomScrollView : ScrollView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun scrollToChild(linesToScroll: Int) {
        // Okamžité posunutí obsahu na zadané souřadnice
        scrollTo(0, linesToScroll)
    }
}
