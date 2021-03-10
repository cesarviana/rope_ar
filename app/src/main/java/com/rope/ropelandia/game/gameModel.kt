package com.rope.ropelandia.game

import android.graphics.drawable.Drawable

data class Level(val mat: Mat = mutableListOf())

typealias Tile = Drawable
typealias MatLayer = Array<Array<Tile>>
typealias Mat = MutableList<MatLayer>
fun Mat.numberOfLines() : Int {
    val noLayer = this.size == 0
    return if(noLayer){
        0
    } else {
        val layer = this[0]
        layer.size
    }
}
