package com.rope.ropelandia.game

import com.rope.ropelandia.model.*

private const val NO_EXECUTION = -2
private const val PRE_EXECUTION = -1

data class Game(val levels: List<Level>) {

    val ropePosition = Position(Square(-1, -1), Position.Direction.UNDEFINED)
    var programBlocks = mutableListOf<Block>()
    private var levelIndex = 0

    val programIsExecuting: Boolean
        get() = executedActionIndex != NO_EXECUTION

    var executedActionIndex = NO_EXECUTION
    val startedActionIndex: Int
        get() = executedActionIndex + 1

    fun updateProgramBlocks(blocks: List<Block>) {
        programBlocks.clear()
        programBlocks.addAll(blocks)
    }

    fun updateRoPEPosition(squareX: Int, squareY: Int) {
        this.ropePosition.square = Square(squareX, squareY)
    }

    fun nextSquareIs(type: String): Boolean {
        val changingSquare = goingForward() || goingBackward()
        if (!hasBlocksToExecute() || !changingSquare) {
            return false
        }
        return try {
            val square = nextSquare()
            val level = currentLevel()
            val collectable = level.collectable[square.line][square.column]
            val path = level.path[square.line][square.column]
            collectable == type || path == type
        } catch (e: Exception) {
            false
        }
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
    private fun turningLeft() = nextBlockToExecute() is LeftBlock
    private fun turningRight() = nextBlockToExecute() is RightBlock

    private fun nextBlockToExecute(): Block {
        val nextIndex = executedActionIndex + 1
        return if (hasBlocksToExecute()) programBlocks[nextIndex] else NULL_BLOCK
    }

    private fun hasBlocksToExecute() = executedActionIndex + 1 < programBlocks.size

    fun startExecution() {
        executedActionIndex = PRE_EXECUTION
    }

    fun endExecution() {
        executedActionIndex = NO_EXECUTION
    }

    fun executeAction() {
        this.ropePosition.square = this.nextSquare()
        this.ropePosition.direction = this.nextDirection()
        executedActionIndex++
    }

    private fun nextDirection(): Position.Direction {
        val direction = this.ropePosition.direction
        return when {
            turningLeft() -> {
                when(direction) {
                    Position.Direction.NORTH -> Position.Direction.WEST
                    Position.Direction.WEST -> Position.Direction.SOUTH
                    Position.Direction.SOUTH -> Position.Direction.EAST
                    Position.Direction.EAST -> Position.Direction.NORTH
                    else -> throw IllegalStateException("The toy face is undefined")
                }
            }
            turningRight() -> {
                when(direction) {
                    Position.Direction.NORTH -> Position.Direction.EAST
                    Position.Direction.WEST -> Position.Direction.NORTH
                    Position.Direction.SOUTH -> Position.Direction.WEST
                    Position.Direction.EAST -> Position.Direction.SOUTH
                    else -> throw IllegalStateException("The toy face is undefined")
                }
            }
            else -> direction
        }
    }

    fun numberOfLines(): Int {
        return currentLevel().collectable.size
    }

    private fun currentLevel() = levels[levelIndex]

    fun currentMat() = arrayOf(
        currentLevel().path,
        currentLevel().collectable
    )

    fun startLine() = currentLevel().startPosition.square.line
    fun startColumn() = currentLevel().startPosition.square.column

}

data class Square(val column: Int, val line: Int) {
    fun north() = Square(column, line - 1)
    fun south() = Square(column, line + 1)
    fun west() = Square(column - 1, line)
    fun east() = Square(column + 1, line)
}

class Level(
    val path: Array<Array<String>>,
    val collectable: Array<Array<String>>,
    val startPosition: Position,
    val expectedCommands: Array<String> = arrayOf()
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
        NORTH, SOUTH, EAST, WEST, UNDEFINED;
        companion object {
            fun fromDegrees(angleDegrees: Double): Direction {
                return when {
                    angleDegrees > 45 && angleDegrees <= 135 -> NORTH
                    angleDegrees > 135 && angleDegrees <= 225 -> WEST
                    angleDegrees > 225 && angleDegrees <= 275 -> SOUTH
                    angleDegrees > 275 && angleDegrees <= 360 -> EAST
                    angleDegrees > 0 && angleDegrees <= 45 -> EAST
                    else -> UNDEFINED
                }
            }
        }
    }
}