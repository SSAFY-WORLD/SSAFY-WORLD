package com.ssafy.world.src.main.photo

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DividerItemDecoration(private val divider: Drawable) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val spanCount = (parent.layoutManager as? GridLayoutManager)?.spanCount ?: 1

        val isTopRow = position < spanCount
        val isLeftColumn = position % spanCount == 0

        outRect.set(
            if (isLeftColumn) 0 else divider.intrinsicWidth,
            if (isTopRow) 0 else divider.intrinsicHeight,
            0,
            0
        )
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        val spanCount = (parent.layoutManager as? GridLayoutManager)?.spanCount ?: 1

        val dividerWidth = divider.intrinsicWidth
        val dividerHeight = divider.intrinsicHeight

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)

            val isLeftColumn = position % spanCount == 0

            if (!isLeftColumn) {
                val left = child.left - dividerWidth
                val top = child.top
                val right = child.left
                val bottom = child.bottom

                divider.setBounds(left, top, right, bottom)
                divider.draw(canvas)
            }

            if (position >= spanCount) {
                val left = child.left
                val top = child.top - dividerHeight
                val right = child.right
                val bottom = child.top

                divider.setBounds(left, top, right, bottom)
                divider.draw(canvas)
            }
        }
    }
}
