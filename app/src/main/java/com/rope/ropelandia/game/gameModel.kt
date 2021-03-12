package com.rope.ropelandia.game

import com.rope.ropelandia.model.BackwardBlock
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.ForwardBlock
import com.rope.ropelandia.model.NULL_BLOCK

private const val NO_EXECUTION = -1

data class Game(val levels: List<Level>) {

    val ropePosition = Position(Square(-1, -1), Position.Direction.UNDEFINED)
    var programBlocks = mutableListOf<Block>()

    private var levelIndex = 0

    val programIsExecuting: Boolean
        get() = executionIndex != NO_EXECUTION

    var executionIndex = NO_EXECUTION

    fun updateProgramBlocks(blocks: List<Block>) {
        programBlocks.clear()
        programBlocks.addAll(blocks)
    }

    fun updateRoPEPosition(squareX: Int, squareY: Int) {
        this.ropePosition.square = Square(squareX, squareY)
    }

    private fun nextSquare(): Square {
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

    private fun goingForward() = nextBlockToExecute() is ForwardBlock
    private fun goingBackward() = nextBlockToExecute() is BackwardBlock

    private fun nextBlockToExecute(): Block {
        val nextIndex = executionIndex + 1
        val noMoreBlocks = programBlocks.size >= nextIndex
        return if (noMoreBlocks) NULL_BLOCK else programBlocks[nextIndex]
    }

    fun startExecution() {
        executionIndex = 0
    }

    fun endExecution() {
        executionIndex = NO_EXECUTION
    }

    fun executeAction() {
        this.ropePosition.square = this.nextSquare()
        executionIndex++
    }

    fun numberOfLines(): Int {
        return currentLevel().collectable.size
    }

    private fun currentLevel() = levels[levelIndex]

    fun currentMat() = arrayOf(
        currentLevel().path,
        currentLevel().collectable
    )
}

data class Square(val x: Int, val y: Int) {
    fun north() = Square(x, y - 1)
    fun south() = Square(x, y + 1)
    fun west() = Square(x + 1, y)
    fun east() = Square(x - 1, y)
}

class Level(
    val path: Array<Array<String>>,
    val collectable: Array<Array<String>>,
    val startPosition: Position
)

data class Position(
    var square: Square,
    var direction: Direction,
    var x: Float = 0f,
    var y: Float = 0f
) {

    fun setCoordinate(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    enum class Direction {
        NORTH, SOUTH, EAST, WEST, UNDEFINED
    }
}