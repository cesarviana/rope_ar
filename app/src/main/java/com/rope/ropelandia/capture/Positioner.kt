package com.rope.ropelandia.capture

import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.BlockFactory
import com.rope.ropelandia.model.PositionBlock

interface BlocksPositioner {
    fun reposition(blocks: List<Block>): List<Block>
}

class ProjectorBlocksPositioner(
    private val targetScreenHeight: Int,
    private val targetScreenWidth: Int
) : BlocksPositioner {

    private var homographyMatrix: HomographyMatrix? = null
    private var adjustedRectangle: Rectangle? = null
    private var proportionWidth: Float = 0.0f
    private var proportionHeight: Float = 0.0f

    override fun reposition(blocks: List<Block>): List<Block> {

        val positionBlocks = blocks.filterIsInstance<PositionBlock>()
        calibrate(positionBlocks)

        return moveBlocks(blocks)
    }

    private fun calibrate(positionBlocks: List<PositionBlock>) {

        if(positionBlocks.size < 4)
            return

        positionBlocks
            .map { Point(it.centerX.toDouble(), it.centerY.toDouble()) }
            .let { positionPoints ->
                PerspectiveRectangle.createFromPoints(positionPoints)
            }.let { perspectiveRectangle ->
                val rectangle = perspectiveRectangle.toRectangle()
                calibrateProportions(rectangle)
                calcHomographyMatrix(perspectiveRectangle, rectangle)

                if(adjustedRectangle == null) {
                    adjustedRectangle = rectangle
                }
            }
    }

    private fun calcHomographyMatrix(
        perspectiveRectangle: PerspectiveRectangle,
        newAdjustedRectangle: Rectangle
    ) {
        homographyMatrix =
            HomographyMatrixCalculator.calculate(perspectiveRectangle, newAdjustedRectangle)
    }

    private fun calibrateProportions(newAdjustedRectangle: Rectangle) {
        proportionWidth = targetScreenWidth / newAdjustedRectangle.width().toFloat()
        proportionHeight = targetScreenHeight / newAdjustedRectangle.height().toFloat()
    }

    private fun moveBlocks(blocks: List<Block>): List<Block> {

        if(adjustedRectangle == null || homographyMatrix == null)
            return blocks

        return blocks.map {
            val point = Point(it.centerX.toDouble(), it.centerY.toDouble())
            val newPoint = PointPositionCalculator.calculatePoint(point, homographyMatrix!!)
            BlockFactory.createBlock(
                it.javaClass,
                (newPoint.x - adjustedRectangle!!.left).toFloat() * proportionWidth,
                (newPoint.y - adjustedRectangle!!.top).toFloat() * proportionHeight,
                it.diameter,
                it.angle
            )
        }
    }
}