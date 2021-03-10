package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.LinearLayout

class GameView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {

    var matView: MatView = MatView(context, null)

    var blocksViews: List<BlockView> = listOf()

    private val centerX by lazy { (width shr 1).toFloat() }
    private val centerY by lazy { (height shr 1).toFloat() }

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        layoutParams = LinearLayout.LayoutParams(width, height)
        matView.layoutParams = LinearLayout.LayoutParams(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            // rotate to image be up for the user
            rotate(180f, centerX, centerY)
            matView.draw(canvas)
            drawProgrammingArea()
            // rotate back
            rotate(-180f, centerX, centerY)
            // the blocks must not be rotated
            drawPath()
            blocksViews.forEach {
                it.draw(canvas)
            }
        }
    }

    private val pathPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
    }
    private val internalPath = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
    }

    private fun Canvas.drawPath() {
        if (blocksViews.isNotEmpty()) {
            blocksViews.forEach {
                drawCircle(it.centerX().toFloat(), it.centerY().toFloat(), 100f, pathPaint)
            }
            blocksViews.forEach {
                drawCircle(it.centerX().toFloat(), it.centerY().toFloat(), 90f, internalPath)
            }
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

    fun setExecuting(actionIndex: Int) {
        blocksViews.forEachIndexed { index, blockView ->
            blockView.state = if (index == actionIndex) {
                BlockView.BlockState.EXECUTING
            } else {
                BlockView.BlockState.PARSED
            }
        }
        invalidate()
    }

    fun hideHighlight() {
        blocksViews.forEach {
            it.state = BlockView.BlockState.PARSED
        }
        invalidate()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    fun setMat(mat: Mat) {
        matView.mat = mat
    }

    fun highlight(index: Int) {
        if(blocksViews.size > index) {
            blocksViews[index].state = BlockView.BlockState.EXECUTING
        }
        invalidate()
    }

    fun squareSize() = matView.squareSize

}

