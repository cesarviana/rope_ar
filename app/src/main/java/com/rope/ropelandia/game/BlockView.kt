package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

class BlockView(context: Context) : View(context) {

    enum class BlockState {
        EXECUTING {
            private val paint: Paint = Paint().apply {
                this.color = Color.YELLOW
                this.style = Paint.Style.STROKE
                this.strokeWidth = 30f
            }

            override fun draw(block: BlockView, canvas: Canvas) {
                val centerX = block.bounds.centerX().toFloat()
                val centerY = block.bounds.centerY().toFloat()
                val angle = Math.toDegrees(block.angle.toDouble()).toFloat()
                canvas.apply {
                    rotate(angle, centerX, centerY)
                    drawCircle(centerX, centerY, 85f, paint)
                    rotate(-angle, centerX, centerY)
                }
            }
        },
        PARSED {
            override fun draw(block: BlockView, canvas: Canvas) {}
        };

        abstract fun draw(block: BlockView, canvas: Canvas)
    }

    var bounds = Rect()
    var angle = 0.0f
    var state = BlockState.PARSED

    fun centerX() = bounds.centerX()
    fun centerY() = bounds.centerY()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            state.draw(this, canvas)
        }
    }
}