package com.example.ropelandia

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

open class Block(
    var centerX: Float,
    var centerY: Float,
    private var diameter: Float,
    val angleRadians: Float
) : Paintable {

    private val paint = Paint().apply {
        color = Color.RED
    }

    override fun paint(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, diameter, paint)
    }

    fun updateOrigin(x: Float, y: Float) {
        centerX -= x
        centerY -= y
    }

    fun updateProportion(proportion: Float) {
        centerX *= proportion
        centerY *= proportion
        diameter *= proportion
    }
}

// 157, 205
const val FORWARD = 327
const val POSITION_BLOCK = 157

//const val RIGHT = 157
const val BACKWARD = 279
const val LEFT = 205

object BlockFactory {
    fun createBlock(
        code: Int,
        centerX: Float,
        centerY: Float,
        diameter: Float,
        angleRadians: Float
    ): Block {
        when (code) {
            FORWARD -> return ForwardBlock(centerX, centerY, diameter, angleRadians)
            POSITION_BLOCK -> return PositionBlock(centerX, centerY, diameter, angleRadians)
        }
        return Block(centerX, centerY, diameter, angleRadians)
    }
}

class ForwardBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    Block(centerX, centerY, diameter, angleRadians) {

    private val paint = Paint().apply {
        color = Color.BLUE
        textSize = 30f
    }

    override fun paint(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, 10f, paint)
    }
}

class BackwardBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    Block(centerX, centerY, diameter, angleRadians) {

    private val paint = Paint().apply {
        val orange = Color.rgb(235, 146, 52)
        color = orange
    }

    override fun paint(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, 10f, paint)
    }
}

class PositionBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    Block(centerX, centerY, diameter, angleRadians) {
    private val paint = Paint().apply {
        color = Color.GREEN
    }

    override fun paint(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, 30f, paint)
    }
}