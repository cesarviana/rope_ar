package com.rope.ropelandia.capture

import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.BlockFactory

interface BlocksPositioner {
    fun reposition(blocks: List<Block>): List<Block>
}

class ProjectorBlocksPositioner : BlocksPositioner {

    var homographyMatrix: HomographyMatrix? = null

    override fun reposition(blocks: List<Block>): List<Block> {

        if (homographyMatrix == null)
            return blocks

        return moveBlocks(blocks, homographyMatrix!!)
    }

    private fun moveBlocks(
        blocks: List<Block>,
        homographyMatrix: HomographyMatrix
    ) =
        blocks.map {
            val point =
                Point(it.centerX.toDouble(), it.centerY.toDouble())
            val newPoint = PointPositionCalculator.calculatePoint(point, homographyMatrix)
            BlockFactory.createBlock(
                it.javaClass,
                newPoint.x.toFloat(),
                newPoint.y.toFloat(),
                it.diameter,
                it.angle
            )
        }

}