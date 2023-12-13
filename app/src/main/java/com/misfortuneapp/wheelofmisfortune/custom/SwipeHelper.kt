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

@SuppressLint("ClickableViewAccessibility")
abstract class SwipeHelper(
    context: Context?,
    private val recyclerView: RecyclerView,
    animate: Boolean?
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

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

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val pos = viewHolder.adapterPosition
        if (swipedPos != pos) recoverQueue.add(swipedPos)
        swipedPos = pos
        buttons = buttonsBuffer[pos] ?: mutableListOf()
        buttonsBuffer.clear()
        swipeThreshold = 0.5f * buttons!!.size * BUTTON_WIDTH
        recoverSwipedItem()
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f * defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0f * defaultValue
    }

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
            translationX = dX * buffer.size * BUTTON_WIDTH / itemView.width
            drawButtons(c, itemView, buffer, pos, translationX)
        }
        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive)
    }

    private fun recoverSwipedItem() {
        while (recoverQueue.isNotEmpty()) {
            val pos = recoverQueue.poll()
            if (pos != null && pos > -1) {
                recyclerView.adapter?.notifyItemChanged(pos)
            }
        }
    }

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

    private fun attachSwipe() {
        ItemTouchHelper(this).attachToRecyclerView(recyclerView)
    }

    abstract fun instantiateUnderlayButton(
        viewHolder: RecyclerView.ViewHolder?,
        underlayButtons: MutableList<UnderlayButton>?
    )

    interface UnderlayButtonClickListener {
        fun onClick(pos: Int)
    }

    class UnderlayButton(
        private val imageResId: Drawable?,
        private val buttonBackgroundColor: Int,
        private val clickListener: UnderlayButtonClickListener
    ) {
        private var pos = 0
        private var clickRegion: RectF? = null

        fun onClick(x: Float, y: Float): Boolean {
            return clickRegion != null && clickRegion!!.contains(x, y).also {
                if (it) clickListener.onClick(pos)
            }
        }

        fun onDraw(canvas: Canvas, rect: RectF, pos: Int) {
            val p = Paint()

            // Draw rounded background
            p.color = buttonBackgroundColor
            canvas.drawRoundRect(rect, 10f, 10f, p)

            // Draw Image
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

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            for (button in buttons as ArrayList<UnderlayButton>) {
                if (button.onClick(e.x, e.y)) break
            }
            return true
        }
    }

    private inner class TouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, e: MotionEvent?): Boolean {
            if (swipedPos < 0) return false
            val point = Point(e!!.rawX.toInt(), e.rawY.toInt())
            val swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos)!!
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
            return false
        }
    }

    companion object {
        const val BUTTON_WIDTH = 150
        private var animate: Boolean? = null
    }
}
