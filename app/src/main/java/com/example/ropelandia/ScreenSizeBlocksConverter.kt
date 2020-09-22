package com.example.ropelandia

import topcodes.TopCode

interface BlocksConverter {
    fun convert(topCodes: List<TopCode>): List<Block>
}

class ScreenSizeBlocksConverter(private val proportion: Float) : BlocksConverter {
    override fun convert(topCodes: List<TopCode>): List<Block> {
        return topCodes.map {
            BlockFactory.createBlock(
                it.code,
                it.centerX * proportion,
                it.centerY * proportion,
                it.diameter * proportion,
                it.orientationInRadians
            )
        }.filterNot { it is PositionBlock }
    }
}

class ProjectorBlocksConverter(private val targetScreenHeight: Int, targetScreenWidth: Int) :
    BlocksConverter {

    override fun convert(topCodes: List<TopCode>): List<Block> {
        val blocks = topCodes.map {
            BlockFactory.createBlock(
                it.code,
                it.centerX,
                it.centerY,
                it.diameter,
                it.orientationInRadians
            )
        }

        val positionBlocks = blocks.filterIsInstance<PositionBlock>()
        val delimitedArea = Board().apply { updatePosition(positionBlocks) }

        val programBlocks = blocks.filterNot { it is PositionBlock }

        val x = delimitedArea.left
        val y = delimitedArea.top

        val proportion = targetScreenHeight / delimitedArea.height()

        programBlocks.forEach {
            it.updateOrigin(x, y)
            it.updateProportion(proportion)
        }

        return programBlocks
    }
}