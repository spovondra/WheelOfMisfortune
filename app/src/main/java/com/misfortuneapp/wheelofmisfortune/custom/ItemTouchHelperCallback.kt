package com.misfortuneapp.wheelofmisfortune.custom

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.misfortuneapp.wheelofmisfortune.R

class ItemTouchHelperCallback(private val adapter: TaskAdapter) :
    ItemTouchHelper.Callback() {

    private val paint = Paint().apply {
        color = Color.RED
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.onItemDismiss(viewHolder.adapterPosition)

        Toast.makeText(
            viewHolder.itemView.context,
            "Task deleted",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        adapter.notifyDataSetChanged()

        super.clearView(recyclerView, viewHolder)
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
        val itemView: View = viewHolder.itemView

        val iconMargin = (itemView.height - R.dimen.icon_size) / 2
        val iconHeight = itemView.height - 2 * iconMargin

        val background: RectF
        val iconDest: RectF

        if (dX > 0) {
            background = RectF(
                itemView.left.toFloat(),
                itemView.top.toFloat(),
                dX,
                itemView.bottom.toFloat()
            )
            iconDest = RectF(
                itemView.left.toFloat() + iconMargin,
                itemView.top.toFloat() + iconMargin,
                itemView.left.toFloat() + iconMargin + iconHeight,
                itemView.bottom.toFloat() - iconMargin
            )
        } else {
            background = RectF(
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            iconDest = RectF(
                itemView.right.toFloat() - iconMargin - iconHeight,
                itemView.top.toFloat() + iconMargin,
                itemView.right.toFloat() - iconMargin,
                itemView.bottom.toFloat() - iconMargin
            )
        }

        c.drawRect(background, paint)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
