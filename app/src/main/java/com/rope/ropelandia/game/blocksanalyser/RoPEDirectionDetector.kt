package com.rope.ropelandia.game.blocksanalyser

import com.rope.ropelandia.game.Position
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.RoPEBlock

abstract class RoPEDirectionDetector : BlocksAnalyzer {

    private var direction = Position.Direction.UNDEFINED

    override fun analyze(blocks: List<Block>) {

        blocks.filterIsInstance<RoPEBlock>().forEach {

            val angleDegrees = Math.toDegrees(it.angle.toDouble()).let { angle ->
                if( angle < 0 ) angle + 360 else angle
            }

            val currentDirection: Position.Direction = when {
                angleDegrees > 45 && angleDegrees <= 135 -> Position.Direction.NORTH
                angleDegrees > 135 && angleDegrees <= 225 -> Position.Direction.WEST
                angleDegrees > 225 && angleDegrees <= 275 -> Position.Direction.SOUTH
                angleDegrees > 275 && angleDegrees <= 360 -> Position.Direction.EAST
                angleDegrees > 0 && angleDegrees <= 45 -> Position.Direction.EAST
                else -> Position.Direction.UNDEFINED
            }

            if (currentDirection != direction) {
                direction = currentDirection
                changedFace(direction)
            }
        }

    }

    abstract fun changedFace(direction: Position.Direction)
}