package com.rope.ropelandia.game

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.URI

typealias GameLoaded = (game: Game) -> Unit

object GameLoader {

    fun load(dataUrl: URI? = null, callback: GameLoaded) {
        Thread {
            loadFromUrlOrDefaultGame(dataUrl, callback)
        }.start()
    }

    private fun loadFromUrlOrDefaultGame(dataUrl: URI?, callback: GameLoaded) {
        if (dataUrl == null) {
            loadDefaultGame(callback)
        } else {
            loadGameFrom(dataUrl, callback)
        }
    }

    private fun loadGameFrom(dataUrl: URI, callback: GameLoaded) {
        val type = object : TypeReference<Participation>() {}
        val participation = jacksonObjectMapper().readValue(dataUrl.toURL(), type)
        val levels: List<Level> = participation.test.items.map {
            it.item
        }
        callback.invoke(Game(levels))
    }

    private fun loadDefaultGame(callback: GameLoaded) {
        val level1 = Level(
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
        val level2 = Level(
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
        val game = Game(listOf(level1, level2))
        callback.invoke(game)
    }
}

private data class Test(val id: Int, val name: String, val items: List<ItemWithId>)
private data class ItemWithId(val id: Int, val item: Item)
private typealias Item = Level

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