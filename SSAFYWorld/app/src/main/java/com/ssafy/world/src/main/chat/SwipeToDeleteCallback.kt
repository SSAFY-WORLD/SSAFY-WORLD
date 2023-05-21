package com.ssafy.world.src.main.chat

import android.content.Context
import android.graphics.Canvas
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.world.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

abstract class SwipeToDeleteCallback(context: Context)
    : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val backgroundColor = ContextCompat.getColor(context, R.color.light_blue)
    private val textColor = ContextCompat.getColor(context, R.color.white)
    // drag & drop 을 사용하는 경우에만 동작
    override fun onMove(recyclerView: RecyclerView,viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }
    // RecyclerView의 onDraw 시 ItemTouchHelper에 호출되며 swipe 이벤트 발생 시 효과 설정
    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY,actionState,isCurrentlyActive)
            .addBackgroundColor(backgroundColor)
            .addSwipeLeftLabel("삭제")
            .setSwipeLeftLabelColor(textColor)
            .setSwipeLeftLabelTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            .create()
            .decorate()

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}