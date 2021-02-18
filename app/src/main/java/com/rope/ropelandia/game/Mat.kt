package com.rope.ropelandia.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.rope.ropelandia.model.Program

private const val BORDER_WIDTH = 15f

class Mat(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private val blocks = mutableListOf<BlockView>()

    var program = Program(listOf())
    set(value) {
        field = value
        blocks.clear()
        program.blocks.forEach {
            blocks.add(BlockView(context, it))
        }
    }

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
        setBackgroundColor(Color.BLACK)
    }

    private val matBorderPaint = Paint().apply{
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = BORDER_WIDTH
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.apply {
            drawMatBorder()
            blocks.forEach {
                it.draw(canvas)
            }
        }
    }

    private fun Canvas.drawMatBorder() {
        val right = width - BORDER_WIDTH * 2
        val bottom = height - BORDER_WIDTH * 2
        drawRect(0f, 0f, right, bottom, matBorderPaint)
    }

    fun hideHighlight() {
        blocks.forEach {
            it.highlighted = false
        }
        updateDraw()
    }

    private fun updateDraw() {
        val canvas = holder.lockCanvas()
        draw(canvas)
        holder.unlockCanvasAndPost(canvas)
    }

    fun highlight(actionIndex: Int) {
        if(blocks.size > actionIndex) {
            val blockView = blocks[actionIndex]
            blockView.highlighted = true
            updateDraw()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

}

