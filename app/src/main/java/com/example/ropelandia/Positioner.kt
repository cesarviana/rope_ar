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
    private var calibrating: Boolean = true

    private var homographyMatrix: HomographyMatrix? = null
    private var adjustedRectangle: Rectangle? = null
    private var proportionWidth: Float = 0.0f
    private var proportionHeight: Float = 0.0f

    override fun reposition(blocks: List<Block>): List<Block> {
        val positionBlocks = blocks.filterIsInstance<PositionBlock>()

        if (calibrating) {
            calibrate(positionBlocks)
        }

        return moveBlocks(blocks)
    }

    private fun calibrate(positionBlocks: List<PositionBlock>) {

        if(positionBlocks.size < 4)
            return

        positionBlocks
            .map { Point(it.x.toDouble(), it.y.toDouble()) }
            .let { positionPoints ->
                createPerspectiveRectangle(positionPoints)
            }.let { perspectiveRectangle ->
                val newAdjustedRectangle = rectangleFinder.adjustRectangle(perspectiveRectangle)
                calibrateProportions(newAdjustedRectangle)
                calcHomographyMatrix(perspectiveRectangle, newAdjustedRectangle)

                if(adjustedRectangle == null) {
                    adjustedRectangle = newAdjustedRectangle
                } else {
                    stopCalibrationIfSimilar(newAdjustedRectangle)
                    adjustedRectangle = newAdjustedRectangle
                }
            }
    }

    private fun stopCalibrationIfSimilar(newAdjustedRectangle: Rectangle) {
        if(adjustedRectangle != null && adjustedRectangle != newAdjustedRectangle) {
            val oldTop = adjustedRectangle!!.topLeft.y
            val newTop = newAdjustedRectangle.topLeft.y

            if(Math.abs(oldTop - newTop) < 3){
                calibrating = false
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
            val point = Point(it.x.toDouble(), it.y.toDouble())
            val newPoint = PointPositionCalculator.calculatePoint(point, homographyMatrix!!)
            BlockFactory.createBlock(
                it.javaClass,
                (newPoint.x - adjustedRectangle!!.topLeft.x).toFloat() * proportionWidth,
                (newPoint.y - adjustedRectangle!!.topLeft.y).toFloat() * proportionHeight,
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