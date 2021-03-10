package com.rope.ropelandia.game

import android.graphics.drawable.Drawable

data class Level(val mat: Mat = mutableListOf())

typealias Tile = Drawable
typealias MatLayer = Array<Array<Tile>>
typealias Mat = MutableList<MatLayer>
