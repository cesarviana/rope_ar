package com.rope.ropelandia

import com.rope.ropelandia.capture.*
import com.rope.ropelandia.model.PositionBlock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.assertj.core.data.Offset
import org.assertj.core.data.Percentage
import org.junit.Test

class HomographyMatrixCalculatorTest {

    @Test
    fun calcNewPoint() {

        val topLeft = Point(2.0, 2.0)
        val topRight = Point(4.0, 2.0)
        val bottomLeft = Point(1.0, 4.0)
        val bottomRight = Point(5.0, 4.0)

        val observedRectangle =
            PerspectiveRectangle(topLeft, topRight, bottomLeft, bottomRight)

        val correctedPoint = Point(1.0, 2.0)
        val perspectivePoint = Point(2.0, 2.0)

        testPointPosition(observedRectangle, perspectivePoint, correctedPoint)

    }

    @Test
    fun calcNewPointGreaterSize() {

        val topLeft = Point(88.72, 34.0)
        val topRight = Point(277.36, 31.88)
        val bottomLeft = Point(16.88, 175.92)
        val bottomRight = Point(331.15, 182.29)

        val observedRectangle =
            PerspectiveRectangle(topLeft, topRight, bottomLeft, bottomRight)

        val perspectivePoint = Point(88.72, 34.0)
        val correctedPoint = Point(16.88, 31.88)
        testPointPosition(observedRectangle, perspectivePoint, correctedPoint)

    }

    private fun testPointPosition(
        observedRectangle: PerspectiveRectangle,
        testPoint: Point,
        targetPoint: Point
    ) {
        val targetRectangle = observedRectangle.toRectangle()
        val homographyMatrix =
            HomographyMatrixCalculator.calculate(observedRectangle, targetRectangle)
        val projectedPoint =
            PointPositionCalculator.calculatePoint(testPoint, homographyMatrix)
        val percentage = Percentage.withPercentage(99.999999)
        assertThat(projectedPoint.x).isCloseTo(targetPoint.x, percentage)
            .withFailMessage("X is not equal")
        assertThat(projectedPoint.y).isCloseTo(targetPoint.y, percentage)
            .withFailMessage("Y is not equal")
    }

    @Test
    fun testMapToScreenWidth() {
        val positionBlocks = listOf(
            PositionBlock(774f, 181f, 0f, 0f),
            PositionBlock(2510f, 115f, 0f, 0f),
            PositionBlock(2843.73f, 1176.42f, 0f, 0f),
            PositionBlock(485.39f, 1125.1f, 0f, 0f),
        )

        val targetScreenHeight = 720
        val targetScreenWidth = 1280

        val blocksPositioner = ProjectorBlocksPositioner(targetScreenHeight, targetScreenWidth)
        val resultBlocks = blocksPositioner.reposition(positionBlocks)

        val resultRectangle = resultBlocks.map { Point(it.centerX.toDouble(), it.centerY.toDouble()) }.let {
            PerspectiveRectangle.createFromPoints(it)
        }

        // assert error relative (width)
        val expectedTopLeftX = 0.0
        val error = resultRectangle.topLeft.x - expectedTopLeftX
        val errorRelativeToWidth = error / targetScreenWidth

        assertThat(errorRelativeToWidth).isLessThan(0.01).withFailMessage("To much difference")


        // assert error in pixels
        val closeness = Offset.offset(7.0)

        val softAssertions = SoftAssertions()
        softAssertions.assertThat(resultRectangle.topLeft.x).isCloseTo(0.0, closeness)
        softAssertions.assertThat(resultRectangle.topLeft.y).isCloseTo(0.0, closeness)
        softAssertions.assertAll()

    }

}