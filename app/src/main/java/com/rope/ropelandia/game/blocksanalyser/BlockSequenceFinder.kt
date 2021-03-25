package com.rope.ropelandia.game.blocksanalyser

import android.util.Log
import com.rope.connection.RoPE
import com.rope.program.SequentialProgram
import com.rope.ropelandia.capture.Circle
import com.rope.ropelandia.capture.Point
import com.rope.ropelandia.model.*
import kotlin.math.cos
import kotlin.math.sin

object BlocksToProgramConverter {
    fun convert(blocks: List<Block>) : RoPE.Program {

        val ropeActions = mutableListOf<RoPE.Action>()

        blocks.map {
            when (it) {
                is ForwardBlock -> RoPE.Action.FORWARD
                is BackwardBlock -> RoPE.Action.BACKWARD
                is LeftBlock -> RoPE.Action.LEFT
                is RightBlock -> RoPE.Action.RIGHT
                else -> RoPE.Action.NULL
            }
        }.toCollection(ropeActions)
        Log.d("PROGRAM_CONVERTER", "Rope actions: ${ropeActions.size}")
        return SequentialProgram(ropeActions)
    }
}

object BlockSequenceFinder {

    private const val SNAP_DISTANCE = 90

    fun findSequence(blocks: List<Block>): List<Block> {

        val remainingBlocks = mutableListOf<Block>().apply { addAll(blocks) }
        val programBlocks = mutableListOf<Block>()

        var block = remainingBlocks.find { it is StartBlock }

        while (block != null) {
            remainingBlocks.remove(block)
            programBlocks.add(block)
            block = findSnappedBlock(remainingBlocks, block)
        }

        removeStartBlock(programBlocks)
        return programBlocks
    }

    private fun removeStartBlock(program: MutableList<Block>) {
        if (program.isNotEmpty())
            program.removeFirst()
    }

    private fun Block.centerPoint(): Point {
        return Point(this.centerX.toDouble(), this.centerY.toDouble())
    }

    private fun findSnappedBlock(blocks: List<Block>, block: Block): Block? {

        val snapCircle = calcSnapCircle(block)

        return blocks.filter { it != block }
            .filterIsInstance<ManipulableBlock>()
            .filter {
                snapCircle.intersect(it.centerPoint())
            }.minByOrNull {
                snapCircle.distance(it.centerPoint())
            }
    }

    private fun calcSnapCircle(block: Block): Circle {
        /**
         * Each next block must be on top of the other. The block angle is 0 (3 hours in the clock),
         * so we need to rotate 90 degrees counterclockwise to find the snapped block.
         */
        val ninetyDegrees = 1.5708f
        val angle = block.angle.toDouble() - ninetyDegrees

        val cos = cos(angle)
        val sin = sin(angle)

        val snapAreaX = (cos * SNAP_DISTANCE + block.centerX).toInt()
        val snapAreaY = (sin * SNAP_DISTANCE + block.centerY).toInt()

        val nextBlockExpectedPoint = Point(snapAreaX.toDouble(), snapAreaY.toDouble())
        return Circle(nextBlockExpectedPoint, SNAP_DISTANCE.toDouble())
    }

}