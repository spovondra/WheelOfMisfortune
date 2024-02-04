package com.misfortuneapp.wheelofmisfortune.custom.guideView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Spannable
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

internal class GuideMessageView(context: Context) : LinearLayout(context) {
    private val mPaint: Paint
    private val mRect: RectF
    private val mTitleTextView: TextView
    private val mContentTextView: TextView
    private var location = IntArray(2)

    init {
        val density = context.resources.displayMetrics.density
        setWillNotDraw(false)
        orientation = VERTICAL
        gravity = Gravity.CENTER
        mRect = RectF()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.strokeCap = Paint.Cap.ROUND
        val padding = (PADDING_SIZE * density).toInt()
        val paddingBottom = (BOTTOM_PADDING_SIZE * density).toInt()
        mTitleTextView = TextView(context)
        mTitleTextView.setPadding(padding, padding, padding, paddingBottom)
        mTitleTextView.gravity = Gravity.CENTER
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TITLE_TEXT_SIZE.toFloat())
        mTitleTextView.setTextColor(Color.BLACK)
        addView(
            mTitleTextView,
            LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        mContentTextView = TextView(context)
        mContentTextView.setTextColor(Color.BLACK)
        mContentTextView.setTextSize(
            TypedValue.COMPLEX_UNIT_DIP,
            DEFAULT_CONTENT_TEXT_SIZE.toFloat()
        )
        mContentTextView.setPadding(padding, paddingBottom, padding, padding)
        mContentTextView.gravity = Gravity.CENTER
        addView(
            mContentTextView,
            LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    fun setTitle(title: String?) {
        if (title == null) {
            removeView(mTitleTextView)
            return
        }
        mTitleTextView.text = title
    }

    fun setContentText(content: String?) {
        mContentTextView.text = content
    }

    fun setContentSpan(content: Spannable?) {
        mContentTextView.text = content
    }

    fun setContentTypeFace(typeFace: Typeface?) {
        mContentTextView.typeface = typeFace
    }

    fun setTitleTypeFace(typeFace: Typeface?) {
        mTitleTextView.typeface = typeFace
    }

    fun setTitleTextSize(size: Int) {
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
    }

    fun setContentTextSize(size: Int) {
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
    }

    fun setColor(color: Int) {
        mPaint.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        getLocationOnScreen(location)
        mRect[paddingLeft.toFloat(), paddingTop.toFloat(), (
                width - paddingRight).toFloat()] = (
                height - paddingBottom
                ).toFloat()
        val density = resources.displayMetrics.density.toInt()
        val radiusSize = RADIUS_SIZE * density
        canvas.drawRoundRect(mRect, radiusSize.toFloat(), radiusSize.toFloat(), mPaint)
    }

    companion object {
        private const val RADIUS_SIZE = 5
        private const val PADDING_SIZE = 10
        private const val BOTTOM_PADDING_SIZE = 5
        private const val DEFAULT_TITLE_TEXT_SIZE = 18
        private const val DEFAULT_CONTENT_TEXT_SIZE = 14
    }
}