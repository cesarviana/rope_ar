package com.rope.ropelandia.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceView
import kotlin.math.cos
import kotlin.math.sin

class Mat(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs) {

    private var highlightIndex: Int = NO_HIGHLIGHT
    var blocks = listOf<Block>()

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
        //holder.setFormat(PixelFormat.TRANSPARENT)
        setBackgroundColor(Color.BLACK)
    }

    private val highlightFill = Paint().apply {
        color = Color.YELLOW
    }

    private val highlightBorderPaint = Paint().apply {
        color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {

            val mustHighlight = highlightIndex != NO_HIGHLIGHT
            val hasBlockToHighlight = blocks.size > highlightIndex

            if (mustHighlight && hasBlockToHighlight) {
                drawBlock(blocks[highlightIndex])
            }

        }
    }

    private fun Canvas.drawBlock(it: Block) {
        val angle = it.angle.toDouble()

        /**
         * Subtract 90º from top code angle to point to top of top code.
         * The arrow symbol is there. Them move x y in that direction, and draw a rectangle
         * in which x, y are almost centered.
         */

        val degreesInRadians90 = Math.toRadians(90.0)
        val anglePointingUpTopCode = angle - degreesInRadians90

        val cos = cos(anglePointingUpTopCode)
        val sin = sin(anglePointingUpTopCode)
        val distance = 80
        val xIcon = (cos * distance + it.x).toInt()
        val yIcon = (sin * distance + it.y).toInt()

        val squareSize = 50
        val rectLeft = xIcon - squareSize
        val rectTop = yIcon - squareSize
        val rectRight = xIcon + squareSize
        val rectBottom = yIcon + squareSize
        val rect = Rect(rectLeft, rectTop, rectRight, rectBottom)
        drawRect(rect, highlightBorderPaint)

        rotate(Math.toDegrees(angle).toFloat())
    }

    fun highlight(highlightIndex: Int) {
        this.highlightIndex = highlightIndex
        invalidate()
    }

    fun hideHighlight() {
        this.highlightIndex = NO_HIGHLIGHT
        invalidate()
    }

    private companion object {
        const val NO_HIGHLIGHT = -1
    }
}

