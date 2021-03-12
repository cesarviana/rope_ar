package com.rope.ropelandia.game

object GameLoader {

    fun load(dataUrl: Any?): Game {

        val level = Level(
            path = arrayOf(
                arrayOf("null","null","null","null","null"),
                arrayOf("null","path","path","null","null"),
                arrayOf("null","path","null","null","null"),
                arrayOf("null","path","null","null","null")
            ),
            collectable = arrayOf(
                arrayOf("null","null","null","null","null"),
                arrayOf("null","null","apple","null","null"),
                arrayOf("null","null","null","null","null"),
                arrayOf("null","null","null","null","null")
            ),
            startPosition = Position(
                Square(1,3),
                direction = Position.Direction.NORTH
            )
        )

        return Game(listOf(level))
    }

}
