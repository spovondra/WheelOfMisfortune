package com.kolecko.koleckonestestiv4

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CircularProgressBar(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val circlePaint = Paint()
    private val arcPaint = Paint()
    private val rectF = RectF()
    private var progress = 0 // Výchozí hodnota 50%


    init {
        circlePaint.isAntiAlias = true
        circlePaint.color = resources.getColor(android.R.color.darker_gray)
        circlePaint.style = Paint.Style.STROKE

        arcPaint.isAntiAlias = true
        arcPaint.color = resources.getColor(android.R.color.holo_green_light)
        arcPaint.style = Paint.Style.STROKE
        arcPaint.strokeCap = Paint.Cap.ROUND

        arcPaint.isAntiAlias = true
        arcPaint.color = resources.getColor(android.R.color.holo_green_light)
        arcPaint.strokeWidth = 22f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        val size = if (width < height) width else height
        val strokeWidth = size * 0.1f // Šířka okrajů, můžete upravit podle svých potřeb
        val radius = (size - strokeWidth) / 2

        rectF.set(width / 2 - radius, height / 2 - radius, width / 2 + radius, height / 2 + radius)

        canvas.drawCircle(width / 2, height / 2, radius, circlePaint)

        // Nakreslete oblouk (průběh) na základě procentuální hodnoty
        val angle = 360 * (progress / 100f)
        canvas.drawArc(rectF, -90f, angle, false, arcPaint)
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate()
    }
}
