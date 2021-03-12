package com.rope.ropelandia.game.blocksanalyser

import android.util.Size
import com.rope.ropelandia.game.Game
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.RoPEBlock
import kotlin.math.abs

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

        val numberOfColumns = screenSize.width / squareSize

        val squareX = it.centerX.toInt() / squareSize
        val squareY = it.centerY.toInt() / squareSize

        /**
         * As the map is showed rotated we need to invert x,y positions.
         */
        val squareXCorrected = abs(squareX - (numberOfColumns - 1))
        val squareYCorrected = abs(squareY - (numberOfLines - 1))

        if (positionChanged(squareXCorrected, squareYCorrected)) {
            notifyChangedSquare(squareXCorrected, squareYCorrected)
        }
    }

    private fun positionChanged(squareX: Int, squareY: Int) =
        squareX != this.squareX || squareY != this.squareY

    private fun notifyChangedSquare(squareX: Int, squareY: Int) {
        this.squareX = squareX
        this.squareY = squareY
        changedSquare(squareX, squareY)
    }

    abstract fun changedSquare(squareX: Int, squareY: Int)
}