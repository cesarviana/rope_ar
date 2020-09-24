package com.example.ropelandia

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceView

class Mat(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs) {

    var blocks = listOf<Block>()

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSPARENT)
    }

    private val paint = Paint().apply {
        this.color = Color.YELLOW
    }
    private val borderMarkers = Paint().apply {
        this.color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            blocks.forEach {
                it.paint(canvas)
            }

            drawTicTacToe(canvas)

            val rectHeight = 50f
            val rectWidth = 130f
            drawRect(0f, 0f, rectWidth, rectHeight, borderMarkers)
            drawRect(0f, height - rectHeight, rectWidth, height.toFloat(), borderMarkers)

        }
    }

    private fun Canvas.drawTicTacToe(canvas: Canvas) {
        var tercoHeight = (height / 3).toFloat()
        canvas.drawRect(0f, tercoHeight, width.toFloat(), tercoHeight + 10, paint)
        tercoHeight *= 2
        canvas.drawRect(0f, tercoHeight, width.toFloat(), tercoHeight + 10, paint)


        var tercoWidth = (width / 3).toFloat()
        canvas.drawRect(tercoWidth, 0f, tercoWidth + 10, height.toFloat(), paint)
        tercoWidth *= 2
        canvas.drawRect(tercoWidth, 0f, tercoWidth + 10, height.toFloat(), paint)
    }

}