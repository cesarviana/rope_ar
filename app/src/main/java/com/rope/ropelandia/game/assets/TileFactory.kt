package com.rope.ropelandia.game.assets

import android.content.Context
import com.rope.ropelandia.game.Square

object TileFactory {
    fun createTile(
        context: Context,
        assetName: String,
        line: Int,
        column: Int,
        height: Int,
        width: Int
    ) : Tile {
        return when (assetName) {
            "apple" -> Apple(context, Square(line, column), height, width)
            "path" -> Path(context, Square(line, column), height, width)
            else -> NullTile(context)
        }
    }
}