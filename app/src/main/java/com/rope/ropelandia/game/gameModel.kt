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
        this.ropePosition.square = Square(squareX, squareY)
    }

    private fun nextSquare() : Square {
        val square = ropePosition.square

        return when {
            goingForward() -> {
                when (ropePosition.direction) {
                    Position.Direction.NORTH -> square.north()
                    Position.Direction.SOUTH -> square.south()
                    Position.Direction.WEST -> square.west()
                    Position.Direction.EAST -> square.east()
                    else -> throw IllegalStateException("The toy face is undefined")
                }
            }
            goingBackward() -> {
                when (ropePosition.direction) {
                    Position.Direction.NORTH -> square.south()
                    Position.Direction.SOUTH -> square.north()
                    Position.Direction.WEST -> square.east()
                    Position.Direction.EAST -> square.west()
                    else -> throw IllegalStateException("The toy face is undefined")
                }
            }
            else -> {
                square
            }
        }
    }

    fun nextPosition(): Mat {
        val nextSquare = this.nextSquare()
        return currentMat().subMat(nextSquare.x, nextSquare.y)
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

    fun executeAction() {
        val nextSquare = this.nextSquare()
        this.ropePosition.square = nextSquare
        executionIndex++
    }
}

data class Square(val x: Int, val y: Int) {
    fun north() = Square(x, y - 1)
    fun south() = Square(x, y + 1)
    fun west() = Square(x + 1, y)
    fun east() = Square(x - 1, y)
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
    var square: Square,
    var direction: Direction,
    var x: Float = 0f,
    var y: Float = 0f
) {

    fun setExactPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    enum class Direction {
        NORTH, SOUTH, EAST, WEST, UNDEFINED
    }
}