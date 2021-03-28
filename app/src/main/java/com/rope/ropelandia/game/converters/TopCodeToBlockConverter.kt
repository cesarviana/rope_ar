package com.rope.ropelandia.game.converters

import com.rope.ropelandia.model.*
import java.lang.IllegalArgumentException

object TopCodeToBlockConverter {

    private const val FORWARD = 327
    private const val POSITION_BLOCK = 31
    private const val RIGHT = 157
    private const val BACKWARD = 279
    private const val LEFT = 205
    private const val START = 227

    private const val ROPE = 103

    fun map(topCodeCode: Int): Class<out Block> {
        return when (topCodeCode) {
            FORWARD -> ForwardBlock::class.java
            BACKWARD -> BackwardBlock::class.java
            LEFT -> LeftBlock::class.java
            RIGHT -> RightBlock::class.java
            START -> StartBlock::class.java
            POSITION_BLOCK -> PositionBlock::class.java
            ROPE -> RoPEBlock::class.java
            else -> throw IllegalArgumentException("Invalid topcode $topCodeCode")
        }
    }
}