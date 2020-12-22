package com.rope.ropelandia.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceView

class Mat(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs) {

    private var highlightIndex: Int = NO_HIGHLIGHT
    var blocks = listOf<Block>()

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
        //holder.setFormat(PixelFormat.TRANSPARENT)
        setBackgroundColor(Color.WHITE)
    }

    private val highlightFill = Paint().apply {
        color = Color.YELLOW
    }

    private val highlightBorderPaint = Paint().apply {
        color = Color.BLUE
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
        drawCircle(it.x, it.y, it.diameter * 1.2f, highlightBorderPaint)
        drawCircle(it.x, it.y, it.diameter, highlightFill)
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

