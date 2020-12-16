package com.rope.ropelandia.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.rope.ropelandia.R
import com.rope.ropelandia.capture.ProgramFactory

class Mat(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs) {

    private var highlightIndex: Int = NO_HIGHLIGHT
    var blocks = listOf<Block>()
        set(value) {
            field = value
            program.clear()
            program.addAll(ProgramFactory.findSequence(blocks))
        }
    private val program = mutableListOf<Block>()

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
        //holder.setFormat(PixelFormat.TRANSPARENT)
        setBackgroundColor(Color.DKGRAY)
    }

    private val blockPaint = Paint().apply {
        color = Color.RED
    }

    private val textPaint = Paint().apply {
        textSize = 50f
        color = Color.BLUE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {

            val mustHighlight = highlightIndex != NO_HIGHLIGHT
            val hasBlockToHighlight = blocks.size > highlightIndex

            if (mustHighlight && hasBlockToHighlight) {
                drawBlock(blocks[highlightIndex])
            } else {
                blocks.forEach { drawBlock(it) }
            }

        }
    }

    private fun Canvas.drawBlock(it: Block) {
        drawCircle(it.x, it.y, it.diameter, blockPaint)
        drawText(it.angle.toString(), it.x + 20, it.y, textPaint)
    }

    fun highlight(highlightIndex: Int) {
        this.highlightIndex = highlightIndex
    }

    private companion object {
        const val NO_HIGHLIGHT = -1
    }
}

