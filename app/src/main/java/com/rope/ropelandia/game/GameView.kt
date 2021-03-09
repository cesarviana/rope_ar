package com.rope.ropelandia.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {

    var matView: MatView = MatView(context, null)

    var blocksViews: List<BlockView> = listOf()

    private val centerX by lazy { (width / 2).toFloat() }
    private val centerY by lazy { (height / 2).toFloat() }

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        clear(canvas)
        canvas?.apply {
            // rotate to image be up for the user
            rotate(180f, centerX, centerY)
            matView.draw(canvas)
            drawProgrammingArea()
            // rotate back
            rotate(-180f, centerX, centerY)
            // the blocks must not be rotated
            blocksViews.forEach {
                it.draw(canvas)
            }
            drawPath()
        }
    }

    private val pathPaint = Paint().apply {
        strokeWidth = 150f
        color = Color.YELLOW
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private fun Canvas.drawPath() {
        if (blocksViews.isNotEmpty()) {
            val path = Path()
            val blockView = blocksViews[0]
            path.moveTo(blockView.centerX().toFloat(), blockView.centerY().toFloat())
            blocksViews.forEach {
                path.lineTo(it.centerX().toFloat(), it.centerY().toFloat())
            }
            drawPath(path, pathPaint)
        }
    }

    private val programmingAreaPaint = Paint().apply {
        color = Color.WHITE
    }
    private val round = 40f

    // rounded white rect on right
    private val programmingArea by lazy {
        val margin = width * 0.01f
        val left = width * 0.65f
        val top = margin
        val right = right - margin
        val bottom = bottom - margin
        RectF(left, top, right, bottom)
    }

    private fun Canvas.drawProgrammingArea() {
        drawRoundRect(programmingArea, round, round, programmingAreaPaint)
    }

    private fun clear(canvas: Canvas?) {
        canvas?.drawColor(0, PorterDuff.Mode.CLEAR)
    }

    fun setExecuting(actionIndex: Int) {
        blocksViews.forEachIndexed { index, blockView ->
            blockView.state = if (index == actionIndex) {
                BlockView.BlockState.EXECUTING
            } else {
                BlockView.BlockState.PARSED
            }
        }
        updateDraw()
    }

    fun hideHighlight() {
        blocksViews.forEach {
            it.state = BlockView.BlockState.PARSED
        }
        updateDraw()
    }

    private fun updateDraw() {
        val canvas = holder.lockCanvas()
        try {
            draw(canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

}

