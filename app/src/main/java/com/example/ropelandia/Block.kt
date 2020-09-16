package com.example.ropelandia

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

open class Block (val centerX: Float, val centerY: Float, private val diameter: Float, val angleRadians: Float) {

    private val ovalPaint = Paint().apply {
        color = Color.RED
    }

    open fun paint(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, diameter, ovalPaint)
    }
}
// 157, 205
const val FORWARD = 327
const val RIGHT = 157
const val BACKWARD = 279
const val LEFT = 205

object BlockFactory {
    fun createBlock(code: Int, centerX: Float, centerY: Float, diameter: Float, angleRadians: Float): Block {
        when(code) {
            FORWARD -> return ForwardBlock(centerX, centerY, diameter, angleRadians)
        }
        return Block(centerX, centerY, diameter, angleRadians)
    }
}

class ForwardBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float)
    : Block(centerX, centerY, diameter, angleRadians) {

    private val ovalPaint = Paint().apply {
        color = Color.BLUE
    }

    override fun paint(canvas: Canvas) {
        val rect = Rect(centerX.toInt(), centerY.toInt(), (centerX + 10).toInt(),
            (centerY + 10).toInt()
        )
        canvas.drawRect(rect, ovalPaint)
    }

}