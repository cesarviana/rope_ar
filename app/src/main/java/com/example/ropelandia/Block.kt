package com.example.ropelandia

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

open class Block(
    val x: Float,
    val y: Float,
    val diameter: Float,
    val angleRadians: Float
) : Paintable {

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    override fun paint(canvas: Canvas) {
        canvas.drawCircle(x, y, diameter, paint)
    }
}

object BlockFactory {
    fun createBlock(
        clazz: Class<out Block>,
        x: Float,
        y: Float,
        diameter: Float,
        angleRadians: Float
    ): Block {
        return clazz.getDeclaredConstructor(
            Float::class.java,
            Float::class.java,
            Float::class.java,
            Float::class.java
        )
            .newInstance(x, y, diameter, angleRadians)
    }
}

class StartBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    Block(centerX, centerY, diameter, angleRadians) {

    private val paint = Paint().apply {
        color = Color.RED
    }

    override fun paint(canvas: Canvas) {
        canvas.drawCircle(x, y, diameter, paint)
    }
}

class PositionBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    Block(centerX, centerY, diameter, angleRadians) {
    private val paint = Paint().apply {
        color = Color.GREEN
    }

    override fun paint(canvas: Canvas) {
        canvas.drawCircle(x, y, 30f, paint)
    }
}



open class DirectionBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    Block(centerX, centerY, diameter, angleRadians) {

    private val paint = Paint().apply {
        color = Color.WHITE
    }

    override fun paint(canvas: Canvas) {
        canvas.drawCircle(x, y, diameter * 1.5f, paint)
    }
}

class ForwardBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    DirectionBlock(centerX, centerY, diameter, angleRadians)

class BackwardBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    DirectionBlock(centerX, centerY, diameter, angleRadians)

class LeftBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    DirectionBlock(centerX, centerY, diameter, angleRadians)

class RightBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    DirectionBlock(centerX, centerY, diameter, angleRadians)