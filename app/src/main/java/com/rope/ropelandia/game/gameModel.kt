package com.rope.ropelandia.game

import androidx.core.view.isVisible
import com.rope.ropelandia.game.tiles.Apple
import com.rope.ropelandia.game.tiles.Tile
import com.rope.ropelandia.model.*

private const val NO_EXECUTION = -2
private const val PRE_EXECUTION = -1

data class Game(val levels: List<Level>) {

    private var levelFinished: (() -> Unit)? = null
    private var gameFinished: (() -> Unit)? = null
    private var goingTo: ((square: Square) -> Unit)? = null

    val ropePosition = Position(Square(-1, -1), Position.Direction.UNDEFINED)
    var programBlocks = mutableListOf<Block>()
    private var levelIndex = 0

    var executedActionIndex = NO_EXECUTION

    val programIsExecuting: Boolean
        get() = executedActionIndex != NO_EXECUTION

    val startedActionIndex: Int
        get() = executedActionIndex + 1

    fun updateProgramBlocks(blocks: List<Block>) {
        programBlocks.clear()
        programBlocks.addAll(blocks)
    }

    fun updateRoPEPosition(squareX: Int, squareY: Int) {
        this.ropePosition.square = Square(squareY, squareX)
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
        notifyIfSquareWillChange()
    }

    fun endExecution() {
        executedActionIndex = NO_EXECUTION
    }

    fun executeAction() {
        notifyIfSquareWillChange()
        notifyLevelOrGameFinished()
        updatePosition()
        executedActionIndex++
    }

    private fun notifyLevelOrGameFinished() {
        if (ateAllApples()) {
            if (hasAnotherLevel())
                levelFinished?.invoke()
            else
                gameFinished?.invoke()
        }
    }

    private fun ateAllApples() = tiles().filterIsInstance<Apple>().none { it.isVisible }

    private fun updatePosition() {
        this.ropePosition.square = this.nextSquare()
        this.ropePosition.direction = this.nextDirection()
    }

    private fun notifyIfSquareWillChange() {
        if (hasBlocksToExecute() && changingSquare()) {
            val square = nextSquare()
            goingTo?.invoke(square)
        }
    }

    private fun changingSquare() = goingForward() || goingBackward()

    private fun nextDirection(): Position.Direction {
        val direction = this.ropePosition.direction
        return when {
            turningLeft() -> {
                when (direction) {
                    Position.Direction.NORTH -> Position.Direction.WEST
                    Position.Direction.WEST -> Position.Direction.SOUTH
                    Position.Direction.SOUTH -> Position.Direction.EAST
                    Position.Direction.EAST -> Position.Direction.NORTH
                    else -> throw IllegalStateException("The toy face is undefined")
                }
            }
            turningRight() -> {
                when (direction) {
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

    fun numberOfLines() = currentLevel().lines
    fun numberOfColumns() = currentLevel().columns

    fun tiles() = currentLevel().tiles

    private fun currentLevel() = levels[levelIndex]

    fun startLine() = currentLevel().startPosition.square.line
    fun startColumn() = currentLevel().startPosition.square.column

    private fun hasAnotherLevel() = levels.size > (levelIndex + 1)
    fun goToNextLevel() {
        require(hasAnotherLevel()) {
            "Do not have more levels to go!"
        }
        levelIndex++
    }

    fun onLevelFinished(function: () -> Unit) {
        this.levelFinished = function
    }

    fun onGoingTo(function: (Square) -> Unit) {
        this.goingTo = function
    }

    fun onGameFinished(function: () -> Unit) {
        this.gameFinished = function
    }

    fun getTilesAt(square: Square) = currentLevel().tilesAt(square)

}

data class Square(val line: Int, val column: Int) {
    fun north() = Square(line - 1, column)
    fun south() = Square(line + 1, column)
    fun west() = Square(line, column - 1)
    fun east() = Square(line, column + 1)
    override fun equals(other: Any?): Boolean {
        return if (other is Square) {
            other.column == this.column && other.line == this.line
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = line
        result = 31 * result + column
        return result
    }
}

class Level(
    val tiles: List<Tile>,
    val startPosition: Position,
    val lines: Int,
    val columns: Int
) {
    fun tilesAt(square: Square): List<Tile> {
        require(square.column <= columns && square.line <= lines)
        return tiles.filter { square == it.square }
    }
}

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