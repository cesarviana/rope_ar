package com.rope.ropelandia.capture

import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.BlockFactory
import com.rope.ropelandia.model.PositionBlock
import kotlin.math.abs

interface BlocksPositioner {
    fun reposition(blocks: List<Block>): List<Block>
}

class ProjectorBlocksPositioner(
    private val targetScreenHeight: Int,
    private val targetScreenWidth: Int
) : BlocksPositioner {

    private val rectangleFinder = RectangleFinder()

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
                createPerspectiveRectangle(positionPoints)
            }.let { perspectiveRectangle ->
                val newAdjustedRectangle = rectangleFinder.adjustRectangle(perspectiveRectangle)
                calibrateProportions(newAdjustedRectangle)
                calcHomographyMatrix(perspectiveRectangle, newAdjustedRectangle)

                if(adjustedRectangle == null) {
                    adjustedRectangle = newAdjustedRectangle
                }
            }
    }

    private fun calcHomographyMatrix(
        perspectiveRectangle: Rectangle,
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
                (newPoint.x - adjustedRectangle!!.topLeft.x).toFloat() * proportionWidth,
                (newPoint.y - adjustedRectangle!!.topLeft.y).toFloat() * proportionHeight,
                it.diameter,
                it.angle
            )
        }
    }

    private fun createPerspectiveRectangle(points: List<Point>): Rectangle {
        require(points.size == 4) { "We need four location points" }

        return points.sortedBy { it.x }.let { sortedPoints ->
            val leftPoints = sortedPoints.subList(0, 2)
            val rightPoints = sortedPoints.subList(2, 4)

            val topLeft = leftPoints.minByOrNull { it.y }
            val bottomLeft = leftPoints.maxByOrNull { it.y }
            val topRight = rightPoints.minByOrNull { it.y }
            val bottomRight = rightPoints.maxByOrNull { it.y }

            Rectangle(topLeft!!, topRight!!, bottomRight!!, bottomLeft!!)
        }
    }
}