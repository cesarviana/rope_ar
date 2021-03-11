package com.rope.ropelandia.game.blocksanalyser

import com.rope.ropelandia.game.Position
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.RoPEBlock

abstract class RoPEFaceDetector : BlocksAnalyzer {

    private var face = Position.Face.UNDEFINED

    override fun analyze(blocks: List<Block>) {

        blocks.filterIsInstance<RoPEBlock>().forEach {

            val angleDegrees = Math.toDegrees(it.angle.toDouble()).let { angle ->
                if( angle < 0 ) angle + 360 else angle
            }

            val currentFace: Position.Face = when {
                angleDegrees > 45 && angleDegrees <= 135 -> Position.Face.NORTH
                angleDegrees > 135 && angleDegrees <= 225 -> Position.Face.WEST
                angleDegrees > 225 && angleDegrees <= 275 -> Position.Face.SOUTH
                angleDegrees > 275 && angleDegrees <= 360 -> Position.Face.EAST
                angleDegrees > 0 && angleDegrees <= 45 -> Position.Face.EAST
                else -> Position.Face.UNDEFINED
            }

            if (currentFace != face) {
                face = currentFace
                changedFace(face)
            }
        }

    }

    abstract fun changedFace(face: Position.Face)
}