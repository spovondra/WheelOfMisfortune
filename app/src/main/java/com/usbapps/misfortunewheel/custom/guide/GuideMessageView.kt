package com.usbapps.misfortunewheel.custom.guide

import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Spannable
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

// Třída pro zobrazování zpráv v průvodci
internal class GuideMessageView(context: Context) : LinearLayout(context) {

    // Instance pro kreslení průvodce
    private val mRect: RectF

    // TextView pro titulek a obsah
    private val mTitleTextView: TextView
    private val mContentTextView: TextView

    // Inicializační blok pro inicializaci objektů při vytvoření instance třídy
    init {
        // Inicializace proměnných
        val density = context.resources.displayMetrics.density
        setWillNotDraw(false)
        orientation = VERTICAL
        gravity = Gravity.CENTER
        mRect = RectF()

        // Nastavení velikosti a vlastností TextView pro titulek
        val padding = (PADDING_SIZE * density).toInt()
        val paddingBottom = (BOTTOM_PADDING_SIZE * density).toInt()
        mTitleTextView = TextView(context)
        mTitleTextView.setPadding(padding, padding, padding, paddingBottom)
        mTitleTextView.gravity = Gravity.CENTER
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TITLE_TEXT_SIZE.toFloat())
        mTitleTextView.setTextColor(Color.WHITE)
        addView(
            mTitleTextView,
            LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Nastavení velikosti a vlastností TextView pro obsah
        mContentTextView = TextView(context)
        mContentTextView.setTextColor(Color.WHITE)
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

    // Metoda pro nastavení titulku
    fun setTitle(title: String?) {
        if (title == null) {
            // Pokud je titulek null, odstraní TextView pro titulek
            removeView(mTitleTextView)
            return
        }
        mTitleTextView.text = title
    }

    // Metoda pro nastavení textu obsahu
    fun setContentText(content: String?) {
        mContentTextView.text = content
    }

    // Metoda pro nastavení formátovaného obsahu
    fun setContentSpan(content: Spannable?) {
        mContentTextView.text = content
    }

    // Metoda pro nastavení typu písma obsahu
    fun setContentTypeFace(typeFace: Typeface?) {
        mContentTextView.typeface = typeFace
    }

    // Metoda pro nastavení typu písma titulku
    fun setTitleTypeFace(typeFace: Typeface?) {
        mTitleTextView.typeface = typeFace
    }

    // Metoda pro nastavení velikosti textu titulku
    fun setTitleTextSize(size: Int) {
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
    }

    // Metoda pro nastavení velikosti textu obsahu
    fun setContentTextSize(size: Int) {
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
    }

    // Statické konstanty pro velikosti paddingu a výchozí velikosti textu
    companion object {
        private const val PADDING_SIZE = 10
        private const val BOTTOM_PADDING_SIZE = 5
        private const val DEFAULT_TITLE_TEXT_SIZE = 18
        private const val DEFAULT_CONTENT_TEXT_SIZE = 14
    }
}
