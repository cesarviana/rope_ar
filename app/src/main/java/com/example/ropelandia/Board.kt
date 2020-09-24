package com.example.ropelandia

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

data class Point(var x: Float = 0f, var y: Float = 0f)

class Board : Paintable {

    var top: Float = 0f
    var bottom: Float = 0f
    var left: Float = 0f
    var right: Float = 0f
    var inclination: Float = 0f

    private val paint = Paint().apply {
        this.color = Color.YELLOW
    }

    fun updatePosition(blocks: List<PositionBlock>) {
        val vertical = blocks.map { it.y }
        vertical.min()?.let { top = it }
        vertical.max()?.let { bottom = it }

        val horizontal = blocks.map { it.x }
        horizontal.min()?.let { left = it }
        horizontal.max()?.let { right = it }

        val topLeft = blocks.minBy { it.y }
        val bottomLeft = blocks.maxBy { it.y }

        if (topLeft != null && bottomLeft != null) {
            val co = kotlin.math.abs(topLeft.x - bottomLeft.x)
            val ca = kotlin.math.abs(topLeft.y - bottomLeft.y)

            inclination = co / ca

        }

    }

    override fun paint(canvas: Canvas) {

    }

    fun height() = bottom - top
    fun width() = right - left
}