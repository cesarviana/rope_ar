package com.rope.ropelandia.game

import android.graphics.drawable.Drawable

data class Game(val levels: List<Level>, val ropePosition: Position) {
    private var levelIndex = 0
    fun currentMat(): Mat = levels[levelIndex].mat
}

data class Level(val mat: Mat = mutableListOf())
data class Tile(val drawable: Drawable, val type: TileType){
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
    for(matLayer in this){
        for(matLine in matLayer){
            for(tile: Tile in matLine) {
                if(tile.type == tileType )
                {
                    return true
                }
            }
        }
    }
    return false
}

data class Position(var squareX: Int, var squareY: Int, var face: Face) {
    enum class Face {
        NORTH, SOUTH, EAST, WEST, UNDEFINED
    }
}