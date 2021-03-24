package com.rope.ropelandia.game

import android.content.Context
import com.rope.ropelandia.ctpuzzle.Item
import com.rope.ropelandia.ctpuzzle.Participation
import com.rope.ropelandia.game.tiles.Tile
import com.rope.ropelandia.game.tiles.TileFactory

object ParticipationToGameConverter {
    fun convert(context: Context, participation: Participation) =
        convertItemsToGame(context, participation.test.items.map { it.item })

    private fun convertItemsToGame(context: Context, items: List<Item>): Game {
        val levels = items.map {
            val tiles = extractTile(context, it)
            val lines = it.collectable.size
            val columns = it.collectable[0].size
            Level(tiles, it.startPosition, lines, columns)
        }
        return Game(levels)
    }

    private fun extractTile(context: Context, it: Item): List<Tile> {
        val assets = mutableListOf<Tile>()
        val lines = it.collectable.size
        val height = context.resources.displayMetrics.heightPixels / lines
        val width = context.resources.displayMetrics.widthPixels / lines
        for (line in 0 until lines) {
            val columns = it.collectable[line].size
            for (column in 0 until columns) {
                val path = convertToTile(context, it.path, line, column, height, width)
                assets.add(path)
                val collectable = convertToTile(context, it.collectable, line, column, height, width)
                assets.add(collectable)
            }
        }
        return assets.toList()
    }

    private fun convertToTile(
        context: Context,
        arrayOfArrays: List<List<String>>,
        line: Int,
        column: Int,
        height: Int,
        width: Int
    ): Tile {
        val name = arrayOfArrays[line][column]
        return TileFactory.createTile(context, name, line, column, height, width)
    }
}
