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

    private val rectangleFinder = RectangleFinder()
    private var proportionWidth: Float = 0.0f
    private var proportionHeight: Float = 0.0f

    override fun reposition(blocks: List<Block>): List<Block> {

        val positionBlocks = blocks
            .filterIsInstance<PositionBlock>()

        if (positionBlocks.size < 4) {
            return blocks
        }

        var adjustedRectangle: Rectangle

        return positionBlocks
            .map { Point(it.x.toDouble(), it.y.toDouble()) }
            .let { positionPoints ->
                createPerspectiveRectangle(positionPoints)
            }.let { perspectiveRectangle ->
                adjustedRectangle = rectangleFinder.adjustRectangle(perspectiveRectangle)
                proportionWidth = targetScreenWidth / adjustedRectangle.width().toFloat()
                proportionHeight = targetScreenHeight / adjustedRectangle.height().toFloat()
                HomographyMatrixCalculator.calculate(perspectiveRectangle, adjustedRectangle)
            }.let { homographyMatrix ->
                moveBlocks(blocks, homographyMatrix, adjustedRectangle, proportionWidth, proportionHeight)
            }
    }

    private fun moveBlocks(
        blocks: List<Block>,
        homographyMatrix: HomographyMatrix,
        adjustedRectangle: Rectangle,
        proportionWidth: Float,
        proportionHeight: Float
    ): List<Block> {
        return blocks.map {
            val point = Point(it.x.toDouble(), it.y.toDouble())
            val newPoint = PointPositionCalculator.calculatePoint(point, homographyMatrix)
            BlockFactory.createBlock(
                it.javaClass,
                (newPoint.x - adjustedRectangle.topLeft.x).toFloat() * proportionWidth,
                (newPoint.y - adjustedRectangle.topLeft.y).toFloat() * proportionHeight,
                it.diameter,
                it.angleRadians
            )
        }
    }

    private fun createPerspectiveRectangle(points: List<Point>): Rectangle {
        require(points.size == 4) { "We need four location points" }

        return points.sortedBy { it.x }.let { sortedPoints ->
            val leftPoints = sortedPoints.subList(0, 2)
            val rightPoints = sortedPoints.subList(2, 4)

            val topLeft = leftPoints.minBy { it.y }
            val bottomLeft = leftPoints.maxBy { it.y }
            val topRight = rightPoints.minBy { it.y }
            val bottomRight = rightPoints.maxBy { it.y }

            Rectangle(topLeft!!, topRight!!, bottomRight!!, bottomLeft!!)
        }
    }
}