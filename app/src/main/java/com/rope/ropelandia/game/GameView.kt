package com.rope.ropelandia.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.rope.ropelandia.game.views.BlockView
import com.rope.ropelandia.game.views.RoPEView

class GameView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {

    private val matView: MatView = MatView(context, null)
    private var programBlocks: List<BlockView> = listOf()
    private val ropeView: RoPEView = RoPEView(context)
    private val startPointView: StartPointView = StartPointView(context)

    private val centerX by lazy { (width shr 1).toFloat() }
    private val centerY by lazy { (height shr 1).toFloat() }

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawBackground()
            // rotate to image be up for the user
            rotate(180f, centerX, centerY)
            matView.draw(canvas)
            startPointView.draw(canvas)
            drawProgrammingArea()
            ropeView.draw(canvas)
            showProgramBlocks()
            programBlocks.forEach {
                it.draw(canvas)
            }
            // rotate back
            rotate(-180f, centerX, centerY)

        }
    }

    private val backgroundPaint = Paint().apply {
        color = Color.YELLOW
    }

    private fun Canvas.drawBackground() {
        drawRect(Rect(0,0,width,height),backgroundPaint)
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

    private fun Canvas.showProgramBlocks() {
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
        val left = width * 0.70f + margin
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
        matView.updateMat(game.assets(), game.numberOfLines(), game.numberOfColumns())
        programBlocks = game.programBlocks.map { block ->
            BlockToBlockView.convert(context, block)
        }
        if(game.programIsExecuting){
            highlight(game.startedActionIndex)
        } else {
            hideHighlight()
        }
        ropeView.x = game.ropePosition.x
        ropeView.y = game.ropePosition.y

        if(game.assets().isNotEmpty()) {
            startPointView.squareSize = game.assets()[0].width
            startPointView.line = game.startLine()
            startPointView.column = game.startColumn()
        }
        invalidate()
    }

}

