package com.rope.ropelandia.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceView

class Program(val blocks: List<Block>) {
    init {
        blocks.forEachIndexed { programIndex, block ->
            block.indexInProgram = programIndex
        }
    }
}

class GameContext(var program: Program, var currentBlock: Int)

class Mat(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs) {

    private val gameContext = GameContext(Program(listOf()), NO_CURRENT_BLOCK)

    var program = Program(listOf())
        set(value) {
            field = value
            gameContext.program = value
        }

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
        setBackgroundColor(Color.BLACK)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {

            program.blocks.forEach {
                it.draw(canvas, gameContext)
            }

        }
    }

    fun highlight(highlightIndex: Int) {
        this.gameContext.currentBlock = highlightIndex
        invalidate()
    }

    fun hideHighlight() {
        this.gameContext.currentBlock = NO_CURRENT_BLOCK
        invalidate()
    }

    private companion object {
        const val NO_CURRENT_BLOCK = -1
    }
}

