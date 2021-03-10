package com.rope.ropelandia.game

import android.util.Size
import com.rope.connection.RoPE
import com.rope.ropelandia.capture.BlocksToProgramConverter
import com.rope.ropelandia.capture.ProgramFactory
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.RoPEBlock
import kotlin.math.abs

interface BlocksAnalyzer {
    fun analyze(blocks: List<Block>)
}

class BlocksAnalyzerComposite : BlocksAnalyzer {

    private val analyzers = mutableListOf<BlocksAnalyzer>()

    override fun analyze(blocks: List<Block>) {
        analyzers.forEach {
            it.analyze(blocks)
        }
    }

    fun addBlocksAnalyzer(analyser: BlocksAnalyzer) {
        analyzers.add(analyser)
    }

}

abstract class ProgramDetector(val rope: RoPE) : BlocksAnalyzer {
    override fun analyze(blocks: List<Block>) {
        /**
         * If rope is executing, there is no need for search another programs.
         */
        if (rope.isExecuting())
            return
        val programBlocks = ProgramFactory.findSequence(blocks)
        if (programBlocks.isNotEmpty()) {
            val ropeProgram = BlocksToProgramConverter.convert(programBlocks)
            onFoundProgramBlocks(programBlocks, ropeProgram)
        }
    }

    abstract fun onFoundProgramBlocks(blocks: List<Block>, program: RoPE.Program)
}

abstract class RoPEMovementsDetector(private val game: Game, private val screenSize: Size) :
    BlocksAnalyzer {

    private var squareX = -1
    private var squareY = -1

    override fun analyze(blocks: List<Block>) {

        blocks.filterIsInstance<RoPEBlock>().forEach {
            val numberOfLines = game.currentMat().numberOfLines()

            val squareSize = screenSize.height / numberOfLines

            val numberOfColumns = screenSize.width / squareSize

            val squareX = it.centerX.toInt() / squareSize
            val squareY = it.centerY.toInt() / squareSize

            /**
             * As the map is showed rotated we need to invert x,y positions.
             */
            val squareYCorrected = abs(squareY - (numberOfLines - 1))
            val squareXCorrected = abs(squareX - (numberOfColumns - 1))

            if (positionChanged(squareXCorrected, squareYCorrected)) {
                notifyChangedSquare(squareXCorrected, squareYCorrected)
            }
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