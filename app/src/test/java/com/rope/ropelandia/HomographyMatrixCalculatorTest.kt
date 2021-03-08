package com.rope.ropelandia

import com.rope.ropelandia.capture.HomographyMatrixCalculator
import com.rope.ropelandia.capture.PerspectiveRectangle
import com.rope.ropelandia.capture.Point
import com.rope.ropelandia.capture.PointPositionCalculator
import org.assertj.core.api.Assertions.assertThat
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

}