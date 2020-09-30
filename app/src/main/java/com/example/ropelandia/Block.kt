package com.example.ropelandia

open class Block(
    val x: Float,
    val y: Float,
    val diameter: Float,
    val angle: Float
) {

    init {
        require(x >= 0)
        require(y >= 0)
    }

//    private val paint = Paint().apply {
//        color = Color.RED
//        strokeWidth = 6f
//        textSize = 20f
//    }
//
//    private val textPaint = Paint().apply {
//        color = Color.WHITE
//        textSize = 45f
//    }
//
//    override fun paint(canvas: Canvas) {
////        canvas.drawCircle(x, y, diameter, paint)
////        canvas.drawText("$angle", x + 40, y, textPaint)
//    }
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