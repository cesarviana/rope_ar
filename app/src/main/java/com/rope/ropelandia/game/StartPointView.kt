package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class StartPointView(context: Context) : View(context) {

    var squareSize: Int = 0
    var line: Int = 0
    var column: Int = 0

    private val paint = Paint().apply {
        color = Color.GREEN
    }

    override fun onDraw(canvas: Canvas) {
        canvas.apply {
            val halfSquare = squareSize / 2f
            val centerX = (column * squareSize) + halfSquare
            val centerY = (line * squareSize) + halfSquare
            drawCircle(centerX, centerY, 20f, paint)
        }
    }

}