package com.example.ropelandia

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.ceil

open class Block(
    val x: Float,
    val y: Float,
    val diameter: Float,
    val angleRadians: Float
) : Paintable {

    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 6f
        textSize = 20f
    }

    private val textPaint = Paint().apply {
        color = Color.RED
        textSize = 40f
    }

    override fun paint(canvas: Canvas) {
        canvas.drawCircle(x, y, diameter, paint)
        val xShow = x.toInt()
        val yShow = y.toInt()
        canvas.drawText("$xShow, $yShow",x+40, y, textPaint)
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
    Block(centerX, centerY, diameter, angleRadians)

class PositionBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    Block(centerX, centerY, diameter, angleRadians)

open class DirectionBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    Block(centerX, centerY, diameter, angleRadians)

class ForwardBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    DirectionBlock(centerX, centerY, diameter, angleRadians)

class BackwardBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    DirectionBlock(centerX, centerY, diameter, angleRadians)

class LeftBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    DirectionBlock(centerX, centerY, diameter, angleRadians)

class RightBlock(centerX: Float, centerY: Float, diameter: Float, angleRadians: Float) :
    DirectionBlock(centerX, centerY, diameter, angleRadians)