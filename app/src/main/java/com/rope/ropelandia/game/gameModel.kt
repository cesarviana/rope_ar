package com.rope.ropelandia.game

import android.util.Log
import androidx.core.view.isVisible
import com.rope.ropelandia.game.tiles.Apple
import com.rope.ropelandia.game.tiles.Tile
import com.rope.ropelandia.model.Block

private const val NO_EXECUTION = -2
private const val PRE_EXECUTION = -1

data class Game(val levels: List<Level>) {

    private var levelFinished: (() -> Unit)? = null
    private var atSquare: ((square: Square) -> Unit)? = null

    val ropePosition = Position(Square(-1, -1), Position.Direction.UNDEFINED)
    var programBlocks = mutableListOf<Block>()
    var levelIndex = 0

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
        val square = Square(squareY, squareX)
        if (this.ropePosition.square != square) {
            this.ropePosition.square = square
            atSquare?.invoke(square)
        }
    }

    fun startExecution() {
        executedActionIndex = PRE_EXECUTION
    }

    fun endExecution() {
        executedActionIndex = NO_EXECUTION
    }

    fun executeAction() {
        executedActionIndex++
    }

    private fun notifyIfLevelFinished() {
        if (ateAllApples())
            levelFinished?.invoke()
    }

    private fun ateAllApples() = tiles().filterIsInstance<Apple>().none { it.isVisible }

    fun numberOfLines() = currentLevel().lines

    fun tiles() = currentLevel().tiles

    private fun currentLevel() = levels[levelIndex]

    fun startLine() = currentLevel().startPosition.square.line
    fun startColumn() = currentLevel().startPosition.square.column

    fun hasAnotherLevel() = levels.size > (levelIndex + 1)
    fun goToNextLevel() {
        require(hasAnotherLevel()) {
            "Do not have more levels to go!"
        }
        levelIndex++
    }

    fun onLevelFinished(function: () -> Unit) {
        this.levelFinished = function
    }

    fun onArrivedAtSquare(function: (Square) -> Unit) {
        this.atSquare = function
    }

    fun getTilesAt(square: Square) = currentLevel().tilesAt(square)

    fun updateCoordinate(x: Float, y: Float) = this.ropePosition.setCoordinate(x, y)

    fun checkLevelFinished() = notifyIfLevelFinished()

}

data class Square(val line: Int, val column: Int)

class Level(
    val tiles: List<Tile>,
    val startPosition: Position,
    val lines: Int,
    val columns: Int
) {
    fun tilesAt(square: Square): List<Tile> {
        val outsideArea = square.column > columns || square.line > lines
        if (outsideArea) {
            Log.w("LEVEL", "The square $square must be inside (columns: $columns, lines: $lines)")
            return listOf()
        }
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