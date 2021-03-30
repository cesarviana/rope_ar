package com.rope.ropelandia.ctpuzzle

import com.rope.ropelandia.game.Position

data class Attempt(val commands: List<String>, val timeInSeconds: Long)

data class ResponseForItem(val attempts: List<Attempt>)

data class Item(
    val path: List<List<String>>,
    val collectable: List<List<String>>,
    val startPosition: Position,
    val expectedCommands: List<String> = listOf()
)

const val DEFAULT_CTPUZZLE_DATA_URL =
    "https://api.ctplatform.playerweb.com.br/test-applications/public/data/328e5336-2536-4da7-b941-e6fa03577f3c"