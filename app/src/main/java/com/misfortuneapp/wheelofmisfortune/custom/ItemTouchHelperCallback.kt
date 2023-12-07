package com.misfortuneapp.wheelofmisfortune.custom

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

// Třída definující chování při přetažení položek v RecyclerView
class ItemTouchHelperCallback(private val adapter: TaskAdapter) :
    ItemTouchHelper.Callback() {

    private val paint = Paint().apply {
        color = Color.RED
    }

    // Metoda pro získání pohybových akcí podporovaných položkou
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT)  // Pohyb pouze vlevo (swipe left)
    }

    // Metoda volaná při pohybu položek (není podporováno)
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    // Metoda volaná při swipu položky
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.onItemDismiss(viewHolder.adapterPosition)

        // Zobrazení upozornění, že úloha byla smazána
        Toast.makeText(
            viewHolder.itemView.context,
            "Task deleted",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Metoda volaná po dokončení operace (např. swipu)
    @SuppressLint("NotifyDataSetChanged")
    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        adapter.notifyDataSetChanged()

        super.clearView(recyclerView, viewHolder)
    }

    // Metoda volaná při kreslení položky během pohybu
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


        // Určení pozadí a ikony v závislosti na směru pohybu
        val background: RectF = if (dX > 0) {
            RectF(
                itemView.left.toFloat(),
                itemView.top.toFloat(),
                dX,
                itemView.bottom.toFloat()
            )
        } else {
            RectF(
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
        }

        // Nakreslení pozadí a ikony
        c.drawRect(background, paint)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
