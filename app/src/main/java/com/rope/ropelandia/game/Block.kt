package com.rope.ropelandia.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import kotlin.math.cos
import kotlin.math.sin

private const val NO_INDEX: Int = -1

open class Block(
    val x: Float,
    val y: Float,
    val diameter: Float,
    val angle: Float,
    /**
     * Position of this block in a program.
     */
    var indexInProgram: Int = NO_INDEX
) {
    init {
        require(x >= 0)
        require(y >= 0)
    }

    private val highlightPaint = Paint().apply {
        color = Color.WHITE
    }

    fun draw(canvas: Canvas, gameContext: GameContext){
        if(gameContext.currentBlock == this.indexInProgram) {
            highlight(canvas)
        }
    }

    private fun highlight(canvas: Canvas) {
        val angle = this.angle.toDouble()

        /**
         * Subtract 90º from top code angle to point to top of top code.
         * The arrow symbol is there. Them move x y in that direction, and draw a rectangle
         * in which x, y are almost centered.
         */

        // canvas.drawCircle(x, y, 10f, highlightPaint)

        val degreesInRadians90 = Math.toRadians(90.0)
        val anglePointingUpTopCode = angle - degreesInRadians90

        val cos = cos(anglePointingUpTopCode)
        val sin = sin(anglePointingUpTopCode)
        val distance = 60
        val xIcon = (cos * distance + this.x).toInt()
        val yCorrectionToUp = 20
        val yIcon = (sin * distance + this.y).toInt() + yCorrectionToUp

        val squareHeight = 60
        val squareWidth = 80
        val rectLeft = xIcon - squareWidth
        val rectTop = yIcon - squareHeight
        val rectRight = xIcon + squareWidth
        val rectBottom = yIcon + squareHeight
        val rect = Rect(rectLeft, rectTop, rectRight, rectBottom)
//        canvas.rotate(Math.toDegrees(angle).toFloat(), x, y)
        canvas.drawRect(rect, highlightPaint)
    }

}

object BlockFactory {
    fun createBlock(
        clazz: Class<out Block>,
        x: Float,
        y: Float,
        diameter: Float,
        angle: Float
    ): Block {
        return clazz.getDeclaredConstructor(
            Float::class.java,
            Float::class.java,
            Float::class.java,
            Float::class.java
        )
            .newInstance(
                x.coerceAtLeast(0f),
                y.coerceAtLeast(0f),
                diameter,
                angle
            )
    }
}

class StartBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    Block(centerX, centerY, diameter, angle)

class PositionBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    Block(centerX, centerY, diameter, angle)

open class DirectionBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    Block(centerX, centerY, diameter, angle)

class ForwardBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    DirectionBlock(centerX, centerY, diameter, angle)

class BackwardBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    DirectionBlock(centerX, centerY, diameter, angle)

class LeftBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    DirectionBlock(centerX, centerY, diameter, angle)

class RightBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    DirectionBlock(centerX, centerY, diameter, angle)