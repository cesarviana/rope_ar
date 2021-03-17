package com.rope.ropelandia.game.blocksanalyser

import android.util.Size
import com.rope.ropelandia.game.Game
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.RoPEBlock
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

abstract class RoPESquareDetector(private val game: Game, private val screenSize: Size) :
    BlocksAnalyzer {

    private var squareX = -1
    private var squareY = -1

    override fun analyze(blocks: List<Block>) {

        blocks.filterIsInstance<RoPEBlock>().forEach {
            detectSquare(it)
        }

    }

    private fun detectSquare(it: RoPEBlock) {
        val numberOfLines = game.numberOfLines()

        val squareSize = screenSize.height / numberOfLines

        val margin = 10f
        // x and y must be inside screen size
        val x = between(margin, screenSize.width.toFloat() - margin, it.centerX)
        val y = between(margin, screenSize.height.toFloat() - margin, it.centerY)

        val squareX = (x / squareSize).toInt()
        val squareY = (y / squareSize).toInt()

        if (positionChanged(squareX, squareY)) {
            notifyChangedSquare(squareX, squareY)
        }
    }

    private fun positionChanged(squareX: Int, squareY: Int) =
        squareX != this.squareX || squareY != this.squareY

    private fun notifyChangedSquare(squareX: Int, squareY: Int) {
        this.squareX = squareX
        this.squareY = squareY
        changedSquare(squareX, squareY)
    }

    private fun between(min: Float, max: Float, value: Float): Float {
        return when {
            value in min..max -> {
                value
            }
            value < min -> {
                min
            }
            else -> {
                max
            }
        }
    }

    abstract fun changedSquare(squareX: Int, squareY: Int)
}