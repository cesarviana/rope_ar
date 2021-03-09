package com.rope.ropelandia.capture

import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.BlockFactory
import com.rope.ropelandia.model.PositionBlock

interface BlocksPositioner {
    fun reposition(blocks: List<Block>): List<Block>
}

class ProjectorBlocksPositioner(
    targetScreenHeight: Int,
    targetScreenWidth: Int
) : BlocksPositioner {

    init {
        require(targetScreenHeight > 0 && targetScreenWidth > 0) {
            "The screen size must be > 0"
        }
    }

    private var proportion: Double = 1.0

    private val targetRectangle by lazy {
        Rectangle(0.0, 0.0, targetScreenWidth.toDouble(), targetScreenHeight.toDouble())
    }

    private var homographyMatrix: HomographyMatrix? = null


    override fun reposition(blocks: List<Block>): List<Block> {

        val positionBlocks = blocks.filterIsInstance<PositionBlock>()
        calibrate(positionBlocks)

        if (homographyMatrix == null)
            return blocks

        return moveBlocks(blocks, proportion, homographyMatrix!!)
    }

    private fun calibrate(positionBlocks: List<PositionBlock>) {

        if (positionBlocks.size < 4)
            return

        val perspectiveRectangle = positionBlocks
            .map { Point(it.centerX.toDouble(), it.centerY.toDouble()) }
            .let { positionPoints ->
                PerspectiveRectangle.createFromPoints(positionPoints)
            }

        proportion = targetRectangle.width() / perspectiveRectangle.bottomWidth()
        // the target rectangle is small than photo taken size, so the proportion must be < 1
        check(proportion < 1) { "The proportion must be < 1." }
        check(proportion > 0) { "The proportion must be > 0." }

        // resize the perspective rectangle so its bottom becomes equals to the target rectangle
        val resizedPerspectiveRectangle = perspectiveRectangle.resize(proportion)
        check(resizedPerspectiveRectangle.bottomWidth() == targetRectangle.width()) {
            "Resized perspective rectangle bottom width: ${resizedPerspectiveRectangle.bottomWidth()} is different from" +
                    "target rectangle width: ${targetRectangle.width()}"
        }

        homographyMatrix = calcHomographyMatrix(resizedPerspectiveRectangle, targetRectangle)
    }

    private fun calcHomographyMatrix(
        perspectiveRectangle: PerspectiveRectangle,
        targetRectangle: Rectangle
    ): HomographyMatrix =
        HomographyMatrixCalculator.calculate(perspectiveRectangle, targetRectangle)


    private fun moveBlocks(
        blocks: List<Block>,
        proportion: Double,
        homographyMatrix: HomographyMatrix
    ) =
        blocks.map {
            val point =
                Point(it.centerX.toDouble() * proportion, it.centerY.toDouble() * proportion)
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