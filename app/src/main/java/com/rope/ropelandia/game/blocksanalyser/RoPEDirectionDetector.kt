package com.rope.ropelandia.game.blocksanalyser

import com.rope.ropelandia.game.Position
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.RoPEBlock

abstract class RoPEDirectionDetector : BlocksAnalyzer {

    private var direction = Position.Direction.UNDEFINED

    override fun analyze(blocks: List<Block>) {

        blocks.filterIsInstance<RoPEBlock>().forEach {

            val angleDegrees = getAngleDegrees(it)

            /**
             * As the map is rotated 180º, the west and east directions must also be inverted.
             */
            var currentDirection = Position.Direction.fromDegrees(angleDegrees)

            currentDirection = when (currentDirection) {
                Position.Direction.SOUTH -> Position.Direction.NORTH
                Position.Direction.NORTH -> Position.Direction.SOUTH
                else -> currentDirection
            }

            if (currentDirection != direction) {
                direction = currentDirection
                changedFace(direction)
            }
        }

    }

    private fun getAngleDegrees(it: RoPEBlock) = Math.toDegrees(it.angle.toDouble()).let { angle ->
        if (angle < 0) angle + 360 else angle
    }


    abstract fun changedFace(direction: Position.Direction)
}