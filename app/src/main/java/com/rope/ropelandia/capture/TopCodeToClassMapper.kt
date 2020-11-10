package com.rope.ropelandia.capture

import com.rope.ropelandia.game.*

object TopCodeToClassMapper {

    private const val FORWARD = 327
    private const val POSITION_BLOCK = 31
    private const val RIGHT = 157
    private const val BACKWARD = 205
    private const val LEFT = 279
    private const val START = 227

    fun map(topCodeCode: Int): Class<out Block> {
        when (topCodeCode) {
            FORWARD -> return ForwardBlock::class.java
            BACKWARD -> return BackwardBlock::class.java
            LEFT -> return LeftBlock::class.java
            RIGHT -> return RightBlock::class.java
            START -> return StartBlock::class.java
            POSITION_BLOCK -> return PositionBlock::class.java
        }
        return Block::class.java
    }
}