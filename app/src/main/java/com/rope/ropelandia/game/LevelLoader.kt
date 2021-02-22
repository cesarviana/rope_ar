package com.rope.ropelandia.game

import android.content.Context

object LevelLoader {

    fun load(applicationContext: Context): List<Level> {

        val mat: Mat = mutableListOf()
        val baseLayer = arrayOf(
            arrayOf("floor", "floor", "floor", "floor"),
            arrayOf("floor", "floor", "floor", "floor"),
            arrayOf("floor", "floor", "floor", "floor"),
            arrayOf("floor", "floor", "floor", "floor")
        )
        val path = arrayOf(
            arrayOf("floor", "floor", "floor", "floor"),
            arrayOf("floor", "floor", "floor", "floor"),
            arrayOf("path", "floor", "floor", "floor"),
            arrayOf("path", "floor", "floor", "floor")
        )
        mat.add(baseLayer)
        mat.add(path)

        val task = Level(mat)

        return listOf(task)
    }

}
