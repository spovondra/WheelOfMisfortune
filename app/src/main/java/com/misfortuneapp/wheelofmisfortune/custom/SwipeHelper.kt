package com.misfortuneapp.wheelofmisfortune.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.LinkedList
import java.util.Queue

/**
 * // Abstraktní třída SwipeHelper poskytuje základní funkcionalitu pro ovládání swipe operací v RecyclerView.
 * // Potomci této třídy implementují metody pro vytvoření tlačítek pod řádkem a posluchač kliknutí na ně.
 */
@SuppressLint("ClickableViewAccessibility")
abstract class SwipeHelper(
    context: Context?,
    private val recyclerView: RecyclerView,
    animate: Boolean?
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    // Odkaz na instance GestureDetector, Mapu tlačítek pod řádkem a frontu pro obnovení stržených položek
    private val gestureDetector: GestureDetector
    private val buttonsBuffer: MutableMap<Int, MutableList<UnderlayButton>?> = HashMap()
    private val recoverQueue: Queue<Int> = LinkedList()
    private var buttons: MutableList<UnderlayButton>? = ArrayList()
    private var swipedPos = -1
    private var swipeThreshold = 0.5f

    init {
        Companion.animate = animate
        buttons = ArrayList()
        gestureDetector = GestureDetector(context, GestureListener())
        recyclerView.setOnTouchListener(TouchListener())
        attachSwipe()
    }

    /**
     * // Metoda volaná při pohybu položky v seznamu. Tato implementace vrací false,
     * // což znamená, že položky nelze přesouvat.
     */
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    /**
     * // Metoda volaná po stržení položky. Ukládá informace o stržené položce,
     * // tlačítkách pod řádkem a spouští obnovení stržených položek.
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val pos = viewHolder.adapterPosition
        if (swipedPos != pos) recoverQueue.add(swipedPos)
        swipedPos = pos
        buttons = buttonsBuffer[pos] ?: mutableListOf()
        buttonsBuffer.clear()
        swipeThreshold = 0.5f * buttons!!.size * getButtonWidth(recyclerView.context)
        recoverSwipedItem()
    }

    /**
     * // Vrací poměrnou hodnotu šířky potřebnou k dosažení práhu pro aktivaci akce.
     */
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    /**
     * // Nastavuje únikovou rychlost pro akci.
     */
    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f * defaultValue
    }

    /**
     * // Nastavuje prah rychlosti, který musí být dosažen pro spuštění akce.
     */
    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0f * defaultValue
    }

    /**
     * // Kreslí tlačítka pod řádkem a volá nadřazenou metodu pro kreslení položky.
     */
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val pos = viewHolder.adapterPosition
        var translationX = dX
        val itemView = viewHolder.itemView
        if (pos < 0) {
            swipedPos = pos
            return
        }
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
            var buffer: MutableList<UnderlayButton>? = buttonsBuffer[pos]
            if (buffer == null) {
                buffer = ArrayList()
                instantiateUnderlayButton(viewHolder, buffer)
                buttonsBuffer[pos] = buffer
            }
            translationX = dX * buffer.size * getButtonWidth(itemView.context) / itemView.width
            drawButtons(c, itemView, buffer, pos, translationX)
        }
        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive)
    }

    /**
     * // Obnovuje stržené položky z fronty.
     */
    private fun recoverSwipedItem() {
        while (recoverQueue.isNotEmpty()) {
            val pos = recoverQueue.poll()
            if (pos != null && pos > -1) {
                recyclerView.adapter?.notifyItemChanged(pos)
            }
        }
    }

    /**
     * // Kreslí tlačítka pod řádkem.
     */
    private fun drawButtons(
        c: Canvas,
        itemView: View,
        buffer: List<UnderlayButton>?,
        pos: Int,
        dX: Float
    ) {
        var right = itemView.right.toFloat()
        val dButtonWidth = -1 * dX / buffer!!.size
        for (button in buffer) {
            val left = right - dButtonWidth
            button.onDraw(
                c,
                RectF(
                    left,
                    itemView.top.toFloat(),
                    right,
                    itemView.bottom.toFloat()
                ),
                pos
            )
            right = left
        }
    }

    /**
     * // Připojí helper pro swipe k RecyclerView.
     */
    private fun attachSwipe() {
        ItemTouchHelper(this).attachToRecyclerView(recyclerView)
    }

    /**
     * // Abstraktní metoda, která je implementována v potomcích pro vytvoření tlačítek pod řádky.
     */
    abstract fun instantiateUnderlayButton(
        ignoredViewHolder: RecyclerView.ViewHolder?,
        underlayButtons: MutableList<UnderlayButton>?
    )

    /**
     * // Rozhraní pro posluchač kliknutí na tlačítko pod řádkem.
     */
    interface UnderlayButtonClickListener {
        fun onClick(pos: Int)
    }

    /**
     * // Třída reprezentující tlačítko pod řádkem.
     */
    class UnderlayButton(
        private val imageResId: Drawable?,
        private val buttonBackgroundColor: Int,
        private val clickListener: UnderlayButtonClickListener
    ) {
        private var pos = 0
        private var clickRegion: RectF? = null

        /**
         * // Metoda volaná při kliknutí na tlačítko.
         */
        fun onClick(x: Float, y: Float): Boolean {
            return clickRegion != null && clickRegion!!.contains(x, y).also {
                if (it) clickListener.onClick(pos)
            }
        }

        /**
         * // Kreslí tlačítko pod řádkem.
         */
        fun onDraw(canvas: Canvas, rect: RectF, pos: Int) {
            val p = Paint()

            // Kreslí obdélník pro tlačítko
            p.color = buttonBackgroundColor
            canvas.drawRoundRect(rect, 10f, 10f, p)

            // Kreslí ikonu
            if (imageResId != null) {
                imageResId.setBounds(
                    (rect.left + 30).toInt(),
                    (rect.top + 40).toInt(),
                    (rect.right - 30).toInt(),
                    (rect.bottom - 40).toInt()
                )
                imageResId.draw(canvas)
            }

            clickRegion = rect
            this.pos = pos
        }
    }

    /**
     * // Vnitřní třída pro zachytávání gest.
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            for (button in buttons as ArrayList<UnderlayButton>) {
                if (button.onClick(e.x, e.y)) break
            }
            return true
        }
    }

    /**
     * // Vnitřní třída pro zachytávání dotyků.
     */
    private inner class TouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, e: MotionEvent?): Boolean {
            if (swipedPos < 0) return false
            val swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos)
            if (swipedViewHolder != null) {
                val point = Point(e!!.rawX.toInt(), e.rawY.toInt())
                val swipedItem = swipedViewHolder.itemView
                val rect = Rect()
                swipedItem.getGlobalVisibleRect(rect)
                if (e.action == MotionEvent.ACTION_DOWN || e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_MOVE) {
                    if (rect.top < point.y && rect.bottom > point.y) {
                        gestureDetector.onTouchEvent(e)
                    } else {
                        recoverQueue.add(swipedPos)
                        swipedPos = -1
                        recoverSwipedItem()
                    }
                }
            }
            return false
        }
    }

    companion object {
        private fun calculateButtonWidth(context: Context): Int {
            // Zde můžete použít jiný způsob pro výpočet šířky, například 1/5 šířky obrazovky
            val displayMetrics = context.resources.displayMetrics
            return (displayMetrics.widthPixels * 0.2).toInt()
        }

        // Metoda pro získání hodnoty BUTTON_WIDTH
        private fun getButtonWidth(context: Context): Int {
            return calculateButtonWidth(context)
        }


        private var animate: Boolean? = null
    }
}