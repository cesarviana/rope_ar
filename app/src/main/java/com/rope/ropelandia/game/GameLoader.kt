package com.rope.ropelandia.game

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import com.rope.ropelandia.R

object GameLoader {

    fun load(applicationContext: Context): Game {

        val floor = Tile(ResourcesCompat.getDrawable(applicationContext.resources, R.drawable.floor, null)!!,Tile.TileType.OFF_ROAD)
        val empty = Tile(ResourcesCompat.getDrawable(applicationContext.resources, R.drawable.empty, null)!!,Tile.TileType.OFF_ROAD)
        val path = Tile(ResourcesCompat.getDrawable(applicationContext.resources, R.drawable.path, null)!!,Tile.TileType.PATH)
        val apple = Tile(ResourcesCompat.getDrawable(applicationContext.resources, R.drawable.apple, null)!!,Tile.TileType.OBSTACLE)

        val floorLayer: MatLayer = arrayOf(
            arrayOf(floor, floor, floor, floor, floor),
            arrayOf(floor, floor, floor, floor, floor),
            arrayOf(floor, floor, floor, floor, floor),
            arrayOf(floor, floor, floor, floor, floor)
        )

        val pathLayer: MatLayer = arrayOf(
            arrayOf(empty, empty, empty, empty, empty),
            arrayOf(empty, path, path, path, empty),
            arrayOf(empty, path, empty, path, empty),
            arrayOf(empty, path, empty, path, empty)
        )

        val applesLayer: MatLayer = arrayOf(
            arrayOf(empty, empty, empty, empty, empty),
            arrayOf(empty, empty, apple, empty, empty),
            arrayOf(empty, apple, empty, empty, empty),
            arrayOf(empty, empty, empty, apple, empty)
        )

        val mat: Mat = arrayListOf()

        mat.add(floorLayer)
        mat.add(pathLayer)
        mat.add(applesLayer)

        val level = Level(mat)

        val ropePosition = Position(-1, -1, Position.Face.UNDEFINED)

        return Game(listOf(level), ropePosition)
    }

}
