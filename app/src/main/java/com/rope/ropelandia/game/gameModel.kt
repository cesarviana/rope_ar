package com.rope.ropelandia.game

import android.graphics.drawable.Drawable

data class Game(val levels: List<Level>) {
    private var levelIndex = 0
    fun currentMat(): Mat = levels[levelIndex].mat
}

data class Level(val mat: Mat = mutableListOf())
typealias Tile = Drawable
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
        if(noLines)
            0
        else
            layer[0].size
    }
}
