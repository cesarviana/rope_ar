package com.example.ropelandia

interface BlocksPositioner {
    fun reposition(blocks: List<Block>): List<Block>
}

class ScreenSizeBlocksPositioner(private val proportion: Float) : BlocksPositioner {
    override fun reposition(blocks: List<Block>): List<Block> {
        return blocks.map {
            BlockFactory.createBlock(
                it.javaClass,
                it.x * proportion,
                it.y * proportion,
                it.diameter * proportion,
                it.angleRadians
            )
        }.filterNot { it is PositionBlock }
    }
}

class ProjectorBlocksPositioner(
    private val targetScreenHeight: Int,
    private val targetScreenWidth: Int
) : BlocksPositioner {

    override fun reposition(blocks: List<Block>): List<Block> {
        return blocks
    }
}