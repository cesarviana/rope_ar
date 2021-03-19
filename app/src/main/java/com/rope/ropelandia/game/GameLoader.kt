package com.rope.ropelandia.game

import android.content.Context
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rope.ropelandia.game.tiles.Tile
import com.rope.ropelandia.game.tiles.TileFactory
import java.lang.Exception
import java.net.URI

typealias GameLoaded = (game: Game) -> Unit

object GameLoader {

    fun load(context: Context, dataUrl: URI? = null, callback: GameLoaded) {
        Thread {
            loadFromUrlOrDefaultGame(context, dataUrl, callback)
        }.start()
    }

    private fun loadFromUrlOrDefaultGame(context: Context, dataUrl: URI?, callback: GameLoaded) {
        if (dataUrl == null) {
            loadDefaultGame(context, callback)
        } else {
            try {
                loadGameFrom(context, dataUrl, callback)
            } catch (e: Exception) {
                loadDefaultGame(context, callback)
            }
        }
    }

    private fun loadGameFrom(context: Context, dataUrl: URI, callback: GameLoaded) {
        val type = object : TypeReference<Participation>() {}
        val participation = jacksonObjectMapper().readValue(dataUrl.toURL(), type)
        val items = participation.test.items.map {
            it.item
        }
        val game = convertItemsToGame(context, items)
        callback.invoke(game)
    }

    private fun loadDefaultGame(context: Context, callback: GameLoaded) {
        val item1 = Item(
            path = arrayOf(
                arrayOf("null", "null", "null", "null", "null"),
                arrayOf("null", "path", "path", "null", "null"),
                arrayOf("null", "path", "null", "null", "null"),
                arrayOf("null", "path", "null", "null", "null")
            ),
            collectable = arrayOf(
                arrayOf("null", "null", "null", "null", "null"),
                arrayOf("null", "null", "null", "null", "null"),
                arrayOf("null", "apple", "null", "null", "null"),
                arrayOf("null", "null", "null", "null", "null")
            ),
            startPosition = Position(
                Square(3, 1),
                direction = Position.Direction.NORTH
            )
        )
        val item2 = Item(
            path = arrayOf(
                arrayOf("null", "null", "null", "null", "null"),
                arrayOf("null", "path", "path", "null", "null"),
                arrayOf("null", "path", "null", "null", "null"),
                arrayOf("null", "path", "null", "null", "null")
            ),
            collectable = arrayOf(
                arrayOf("null", "null", "null", "null", "null"),
                arrayOf("null", "null", "apple", "null", "null"),
                arrayOf("null", "null", "null", "null", "null"),
                arrayOf("null", "null", "null", "null", "null")
            ),
            startPosition = Position(
                Square(3, 1),
                direction = Position.Direction.NORTH
            )
        )
        val game = convertItemsToGame(context, listOf(item1, item2))
        callback.invoke(game)
    }

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
        arrayOfArrays: Array<Array<String>>,
        line: Int,
        column: Int,
        height: Int,
        width: Int
    ): Tile {
        val name = arrayOfArrays[line][column]
        return TileFactory.createTile(context, name, line, column, height, width)
    }
}

private data class Test(val id: Int, val name: String, val items: List<ItemWithId>)
private data class ItemWithId(val id: Int, val item: Item)

class Item(
    val path: Array<Array<String>>,
    val collectable: Array<Array<String>>,
    val startPosition: Position,
    val expectedCommands: Array<String> = arrayOf()
)

private data class Participation(
    val participationId: Int,
    val lastVisitedId: Int,
    val test: Test,
    val lastVisitedItemId: Int,
    val urlToSendResponses: UrlToSaveResponse,
    val urlToSendProgress: UrlToSendProgress
)

private open class UrlToSendProgress(val method: String, val url: String, val help: String)
private class UrlToSaveResponse(
    method: String,
    url: String,
    help: String,
    val responseClass: String
) :
    UrlToSendProgress(method, url, help)