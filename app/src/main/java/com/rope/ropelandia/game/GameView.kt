package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

private const val BORDER_WIDTH = 10f

class GameView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    lateinit var blocksViews: List<BlockView>
    lateinit var matView: MatView

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
            blocksViews.forEach {
                it.draw(canvas)
            }
        }
    }

    private fun Canvas.drawMatBorder() {
        val right = width - BORDER_WIDTH
        val bottom = height - BORDER_WIDTH
        drawRect(0f, 0f, right, bottom, matBorderPaint)
    }

    fun hideHighlight() {
        blocksViews.forEach {
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
        if(blocksViews.size > actionIndex) {
            val blockView = blocksViews[actionIndex]
            blockView.highlighted = true
            updateDraw()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

}

