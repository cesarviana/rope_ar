package com.rope.ropelandia.model

import kotlin.math.cos
import kotlin.math.sin

open class Block(
    var centerX: Float,
    var centerY: Float,
    val diameter: Float,
    val angle: Float
) {
    init {
        require(centerX >= 0) { "Block center X must be > 0" }
        require(centerY >= 0) { "Block center Y must be > 0" }
    }

    companion object {
        const val WIDTH = 180
        const val HEIGHT = 150
    }

    val left = centerX - (WIDTH shr 1)
    val top = centerY - (HEIGHT shr 1)
    val right = centerX + (WIDTH shr 1)
    val bottom = centerY + (HEIGHT shr 1)
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

class PositionBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    Block(centerX, centerY, diameter, angle) {
    init {
        /**
         * Subtract 270º from top code angle to point to center of rope head.
         */
        val anglePointingToyHead = angle - Math.toRadians(270.0)

        val cos = cos(anglePointingToyHead)
        val sin = sin(anglePointingToyHead)
        val correction = 40
        this.centerX = (cos * correction + centerX).toFloat()
        this.centerY = (sin * correction + centerY).toFloat()
    }
}

class RoPEBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    Block(centerX, centerY, diameter, angle)

open class ManipulableBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    Block(centerX, centerY, diameter, angle) {

    init {
        /**
         * Subtract 180º from top code angle to point to top code.
         * The center of the block is there.
         * 180º is equal to Math.PI radians.
         */
        val anglePointingUpTopCode = angle - Math.PI

        val cos = cos(anglePointingUpTopCode)
        val sin = sin(anglePointingUpTopCode)
        val correction = 60
        this.centerX = (cos * correction + centerX).toFloat()
        this.centerY = (sin * correction + centerY).toFloat()
    }
}

class StartBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    ManipulableBlock(centerX, centerY, diameter, angle)

class ForwardBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    ManipulableBlock(centerX, centerY, diameter, angle)

class BackwardBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    ManipulableBlock(centerX, centerY, diameter, angle)

class LeftBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    ManipulableBlock(centerX, centerY, diameter, angle)

class RightBlock(centerX: Float, centerY: Float, diameter: Float, angle: Float) :
    ManipulableBlock(centerX, centerY, diameter, angle)