package com.rope.ropelandia.game

import android.graphics.drawable.Drawable
import com.rope.ropelandia.model.BackwardBlock
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.ForwardBlock

private const val NO_EXECUTION = -1

data class Game(val levels: List<Level>, val ropePosition: Position) {
    var programBlocks = mutableListOf<Block>()
    private var levelIndex = 0
    fun currentMat(): Mat = levels[levelIndex].mat

    val programIsExecuting: Boolean
        get() = executionIndex != NO_EXECUTION

    var executionIndex = NO_EXECUTION

    fun updateProgramBlocks(blocks: List<Block>) {
        programBlocks.clear()
        programBlocks.addAll(blocks)
    }

    fun updateRoPESquare(squareX: Int, squareY: Int) {
        this.ropePosition.squareX = squareX
        this.ropePosition.squareY = squareY
    }

    fun nextPosition(): Mat {
        val x = ropePosition.squareX
        val y = ropePosition.squareY

        return when {
            goingForward() -> {
                when (ropePosition.face) {
                    Position.Face.NORTH -> currentMat().subMat(x, y - 1)
                    Position.Face.SOUTH -> currentMat().subMat(x, y + 1)
                    Position.Face.WEST -> currentMat().subMat(x - 1, y)
                    Position.Face.EAST -> currentMat().subMat(x + 1, y)
                    else -> throw IllegalStateException("The toy face is undefined")
                }
            }
            goingBackward() -> {
                when (ropePosition.face) {
                    Position.Face.NORTH -> currentMat().subMat(x, y + 1)
                    Position.Face.SOUTH -> currentMat().subMat(x, y - 1)
                    Position.Face.WEST -> currentMat().subMat(x + 1, y)
                    Position.Face.EAST -> currentMat().subMat(x - 1, y)
                    else -> throw IllegalStateException("The toy face is undefined")
                }
            }
            else -> {
                currentMat().subMat(x, y)
            }
        }
    }

    private fun goingForward() = hasBlockToExecute() && nextBlockToExecute() is ForwardBlock
    private fun goingBackward() = hasBlockToExecute() && nextBlockToExecute() is BackwardBlock

    private fun nextBlockToExecute() = programBlocks[executionIndex + 1]
    private fun hasBlockToExecute() = programBlocks.size > executionIndex + 1

    fun startExecution() {
        executionIndex = 0
    }

    fun endExecution() {
        executionIndex = NO_EXECUTION
    }
}

data class Level(val mat: Mat = mutableListOf())
data class Tile(val drawable: Drawable, val type: TileType) {
    enum class TileType {
        OBSTACLE, COLLECTABLE, PATH, OFF_ROAD
    }
}
typealias MatLayer = Array<Array<Tile>>
typealias Mat = MutableList<MatLayer>

fun Mat.numberOfLines(): Int {
    val noLayer = this.size == 0
    return if (noLayer) {
        0
    } else {
        val layer = this[0]
        layer.size
    }
}

fun Mat.numberOfColumns(): Int {
    val noLayer = this.isEmpty()
    return if (noLayer) {
        0
    } else {
        val layer = this[0]
        val noLines = layer[0].isEmpty()
        if (noLines)
            0
        else
            layer[0].size
    }
}

fun Mat.subMat(x: Int, y: Int): Mat {
    val subMat = mutableListOf<MatLayer>()
    this.forEach { layer: MatLayer ->
        val subMatLayer: MatLayer = arrayOf(
            arrayOf(
                layer[x][y]
            )
        )
        subMat.add(subMatLayer)
    }
    return subMat
}

fun Mat.hasTile(tileType: Tile.TileType): Boolean {
    for (matLayer in this) {
        for (matLine in matLayer) {
            for (tile: Tile in matLine) {
                if (tile.type == tileType) {
                    return true
                }
            }
        }
    }
    return false
}

data class Position(
    var squareX: Int,
    var squareY: Int,
    var face: Face,
    var x: Float = 0f,
    var y: Float = 0f
) {

    fun setExactPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    enum class Face {
        NORTH, SOUTH, EAST, WEST, UNDEFINED
    }
}