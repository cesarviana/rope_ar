package com.rope.ropelandia.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.LinearLayout
import com.rope.ropelandia.game.views.BlockView
import com.rope.ropelandia.game.views.RoPEView
import kotlinx.android.synthetic.main.main_activity.view.*

class GameView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {

    private val matView: MatView = MatView(context, null)
    private var programBlocks: List<BlockView> = listOf()
    private val ropeView: RoPEView = RoPEView(context)

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
            programBlocks.forEach {
                it.draw(canvas)
            }
            ropeView.draw(canvas)
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
        if (programBlocks.isNotEmpty()) {
            programBlocks.forEach {
                drawCircle(it.centerX().toFloat(), it.centerY().toFloat(), 100f, pathPaint)
            }
            programBlocks.forEach {
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

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    private fun highlight(actionIndex: Int) {
        programBlocks.forEachIndexed { index, blockView ->
            blockView.state = if (index == actionIndex) {
                BlockView.BlockState.EXECUTING
            } else {
                BlockView.BlockState.PARSED
            }
        }
    }

    private fun hideHighlight() {
        programBlocks.forEach {
            it.state = BlockView.BlockState.PARSED
        }
    }

    fun update(game: Game) {
        matView.mat = game.currentMat()
        programBlocks = game.programBlocks.map { block ->
            BlockToBlockView.convert(context, block)
        }
        if(game.programIsExecuting){
            highlight(game.executionIndex)
        } else {
            hideHighlight()
        }
        ropeView.x = game.ropePosition.x
        ropeView.y = game.ropePosition.y
        val squareX = game.ropePosition.squareX
        val squareY = game.ropePosition.squareY
        ropeView.bounds = createRect(matView.squareSize, squareY, squareX)
        invalidate()
    }

    private fun createRect(squareSize: Int, matLine: Int, matColumn: Int): Rect {
        val left = matColumn * squareSize
        val top = matLine * squareSize
        val right = left + squareSize
        val bottom = top + squareSize
        return Rect(left, top, right, bottom)
    }

}

