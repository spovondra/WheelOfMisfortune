package com.misfortuneapp.wheelofmisfortune.custom

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
        initializePaints()
    }

    private fun initializePaints() {
        // Nastavení vlastností pro kreslení okraje kruhu
        circlePaint.isAntiAlias = true
        circlePaint.color = ContextCompat.getColor(context, android.R.color.darker_gray) // Barva okraje (opravena deprecated metoda)
        circlePaint.style = Paint.Style.STROKE  // Styl kreslení (okraje)

        // Nastavení vlastností pro kreslení průběhu
        arcPaint.isAntiAlias = true
        arcPaint.color = ContextCompat.getColor(context, android.R.color.holo_green_light) // Barva průběhu (opravena deprecated metoda)
        arcPaint.style = Paint.Style.STROKE  // Styl kreslení (průběhu)
        arcPaint.strokeCap = Paint.Cap.ROUND  // Zakončení průběhu zaoblením
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Získání rozměrů view
        val width = width.toFloat()
        val height = height.toFloat()

        // Určení velikosti view
        val size = if (width < height) width else height
        adjustStrokeWidth(size)

        // Výpočet poloměru a obdélníku pro kreslení průběhu
        val strokeWidth = size * 0.1f
        val radius = (size - strokeWidth) / 2
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

    // Metoda pro nastavení dynamické šířky pruhu na základě rozměrů obrazovky
    private fun adjustStrokeWidth(size: Float) {
        arcPaint.strokeWidth = size * 0.025f
    }
}
