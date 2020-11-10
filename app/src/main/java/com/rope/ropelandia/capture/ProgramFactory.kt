package com.rope.ropelandia.capture

import com.rope.ropelandia.game.Block
import com.rope.ropelandia.game.DirectionBlock
import com.rope.ropelandia.game.StartBlock
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object ProgramFactory {

    private const val SNAP_DISTANCE = 140

    fun createProgram(blocks: List<Block>): List<Block> {

        val program = mutableListOf<Block>()

        var block = blocks.find { it is StartBlock }

        while (block != null) {
            program.add(block)
            val remainingBlocks = blocks.filterNot { program.contains(it) }
            block = findSnappedBlock(remainingBlocks, block)
        }

        return program
    }

    private fun findSnappedBlock(blocks: List<Block>, block: Block): Block? {
        return blocks.filter { it != block }
            .filterIsInstance<DirectionBlock>()
            .find {
                intersect(block, it)
            }
    }

    private fun intersect(
        block: Block,
        block2: Block
    ): Boolean {
        val angle = block.angle.toDouble()

        val cos = cos(angle)
        val sin = sin(angle)

        val snapAreaX = (cos * SNAP_DISTANCE + block.x).toInt()
        val snapAreaY = (sin * SNAP_DISTANCE + block.y).toInt()

        val xSnapDistance = abs(block2.x - snapAreaX)
        val ySnapDistance = abs(block2.y - snapAreaY)

        val intersectHorizontally = xSnapDistance < SNAP_DISTANCE
        val intersectVertically = ySnapDistance < SNAP_DISTANCE

        return intersectHorizontally && intersectVertically
    }
}