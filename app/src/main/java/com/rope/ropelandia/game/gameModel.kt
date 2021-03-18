package com.rope.ropelandia.game

import com.rope.ropelandia.model.*

private const val NO_EXECUTION = -2
private const val PRE_EXECUTION = -1
private val NULL_ASSET = Asset(-1, -1, listOf(), null)

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
        this.ropePosition.square = Square(squareY, squareX)
    }

    fun nextAsset(): Asset {
        val changingSquare = goingForward() || goingBackward()
        if (!hasBlocksToExecute() || !changingSquare) {
            return NULL_ASSET
        }
        return try {
            val square = nextSquare()
            val level = currentLevel()
            val collectable = level.collectable[square.line][square.column]
            val path = level.path[square.line][square.column]
            Asset(square.line, square.column, listOf(collectable, path), this)
        } catch (e: Exception) {
            NULL_ASSET
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

    fun numberOfLines(): Int {
        return currentLevel().collectable.size
    }

    fun currentLevel() = levels[levelIndex]

    fun currentMat() = arrayOf(
        currentLevel().path,
        currentLevel().collectable
    )

    fun startLine() = currentLevel().startPosition.square.line
    fun startColumn() = currentLevel().startPosition.square.column
    fun tookAll(assetType: AssetType) =
        currentLevel().collectable.flatMap { it.toList() }.none { it.isAn(assetType) }

    fun hasAnotherLevel() = levels.size > (levelIndex + 1)
    fun goToNextLevel() {
        require(hasAnotherLevel()) {
            "Do not have more levels to go!"
        }
        levelIndex++
    }

}

data class Asset(val line: Int, val column: Int, val contents: List<String>, val game: Game?) {
    fun isAn(assetType: AssetType) = contents.any { it.isAn(assetType) }
    fun disappear() {
        game?.let {
            it.currentLevel().collectable[line][column] = ""
        }
    }
}

enum class AssetType { APPLE }

data class Square(val line: Int, val column: Int) {
    fun north() = Square(line - 1, column)
    fun south() = Square(line + 1, column)
    fun west() = Square(line, column - 1)
    fun east() = Square(line, column + 1)
}

typealias TileName = String

fun TileName.isAn(assetType: AssetType): Boolean {
    return this.toUpperCase() == assetType.name
}

class Level(
    val path: Array<Array<TileName>>,
    val collectable: Array<Array<TileName>>,
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