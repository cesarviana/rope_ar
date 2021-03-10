package com.rope.ropelandia.capture

import com.rope.ropelandia.model.*

object TopCodeToClassMapper {

    private const val FORWARD = 327
    private const val POSITION_BLOCK = 31
    private const val RIGHT = 157
    private const val BACKWARD = 2790000 // TODO use 279 for backward and get another code for ROPE_BLOCK
    private const val LEFT = 205
    private const val START = 227

    private const val ROPE = 279

    fun map(topCodeCode: Int): Class<out Block> {
        when (topCodeCode) {
            FORWARD -> return ForwardBlock::class.java
            BACKWARD -> return BackwardBlock::class.java
            LEFT -> return LeftBlock::class.java
            RIGHT -> return RightBlock::class.java
            START -> return StartBlock::class.java
            POSITION_BLOCK -> return PositionBlock::class.java
            ROPE -> return RoPEBlock::class.java
        }
        return Block::class.java
    }
}