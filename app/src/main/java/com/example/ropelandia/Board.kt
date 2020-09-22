package com.example.ropelandia

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Board : Paintable {
    var top: Float = 0f
    var bottom: Float = 0f
    var left: Float = 0f
    var right: Float = 0f

    private val paint = Paint().apply {
        this.color = Color.YELLOW
    }

    fun updatePosition(blocks: List<PositionBlock>) {
        val vertical = blocks.map { it.centerY }
        vertical.min()?.let { top = it }
        vertical.max()?.let { bottom = it }

        val horizontal = blocks.map { it.centerX }
        horizontal.min()?.let { left = it }
        horizontal.max()?.let { right = it }
    }

    override fun paint(canvas: Canvas) {
        canvas.drawRect(left, top, right, bottom, paint)
    }

    fun height() = bottom - top
    fun width() = right - left
}