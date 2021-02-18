package com.rope.ropelandia.capture

import com.rope.ropelandia.model.*
import kotlinx.android.synthetic.main.activity_study.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object ProgramFactory {

    private const val SNAP_DISTANCE = 100

    fun findSequence(blocks: List<Block>): Program {

        val blocksList = mutableListOf<Block>()

        var block = blocks.find { it is StartBlock }

        while (block != null) {
            blocksList.add(block)
            val remainingBlocks = blocks.filterNot { blocksList.contains(it) }
            block = findSnappedBlock(remainingBlocks, block)
        }

        removeStartBlock(blocksList)

        return Program(blocksList)
    }

    private fun removeStartBlock(program: MutableList<Block>) {
        if(program.isNotEmpty())
            program.removeFirst()
    }

    private fun findSnappedBlock(blocks: List<Block>, block: Block): Block? {
        return blocks.filter { it != block }
            .filterIsInstance<ManipulableBlock>()
            .find {
                intersect(block, it)
            }
    }

    private fun intersect(
        block: Block,
        block2: Block
    ): Boolean {

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

        val xSnapDistance = abs(block2.centerX - snapAreaX)
        val ySnapDistance = abs(block2.centerY - snapAreaY)

        val intersectHorizontally = xSnapDistance < SNAP_DISTANCE
        val intersectVertically = ySnapDistance < SNAP_DISTANCE

        return intersectHorizontally && intersectVertically
    }
}