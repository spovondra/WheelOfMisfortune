package com.kolecko.koleckonestesti

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class CircularProgressBar(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val circlePaint = Paint()   // Paint pro kreslení kruhu (okraje)
    private val arcPaint = Paint()      // Paint pro kreslení oblouku (průběhu baru)
    private val rectF = RectF()         // Obdélník, do kterého bude nakreslen oblouk
    private var progress = 0            // Výchozí hodnota postupu (v procentech)

    init {
        // Nastavení vlastností pro kreslení okraje kruhu
        circlePaint.isAntiAlias = true
        circlePaint.color = ContextCompat.getColor(context, android.R.color.darker_gray) // Barva okraje (opravena deprecated metoda)
        circlePaint.style = Paint.Style.STROKE  // Styl kreslení (okraje)

        // Nastavení vlastností pro kreslení průběhu
        arcPaint.isAntiAlias = true
        arcPaint.color = ContextCompat.getColor(context, android.R.color.holo_green_light) // Barva průběhu (opravena deprecated metoda)
        arcPaint.style = Paint.Style.STROKE  // Styl kreslení (průběhu)
        arcPaint.strokeCap = Paint.Cap.ROUND  // Zakončení průběhu zaoblením

        // Nastavení vlastností pro kreslení průběhu
        arcPaint.strokeWidth = 22f  // Šířka průběhu
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Získání rozměrů view
        val width = width.toFloat()
        val height = height.toFloat()

        // Určení velikosti view
        val size = if (width < height) width else height
        val strokeWidth = size * 0.1f // Šířka okrajů, můžete upravit podle svých potřeb
        val radius = (size - strokeWidth) / 2

        // Nastavení obdélníku pro kreslení průběhu
        rectF.set(width / 2 - radius, height / 2 - radius, width / 2 + radius, height / 2 + radius)

        // Kreslení kruhu (okraje)
        canvas.drawCircle(width / 2, height / 2, radius, circlePaint)

        // Kreslení oblouku (průběhu) na základě procentuální hodnoty
        val angle = 360 * (progress / 100f)
        canvas.drawArc(rectF, -90f, angle, false, arcPaint)
    }

    // Metoda pro nastavení aktuální hodnoty postupu
    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate()  // Znovu vykreslit view po změně hodnoty
    }
}