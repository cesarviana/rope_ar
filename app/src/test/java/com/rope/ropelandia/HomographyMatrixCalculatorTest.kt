package com.rope.ropelandia

import com.rope.ropelandia.capture.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HomographyMatrixCalculatorTest {

    private val top = 5.0
    private val right = 15.0
    private val left = 5.0
    private val bottom = 20.0

    private val topLeft = Point(left, top)
    private val topRight = Point(right, top)
    private val bottomRight = Point(20.0, bottom)
    private val bottomLeft = Point(0.0, 15.0)

    private val observedRectangle = Rectangle(topLeft, topRight, bottomRight, bottomLeft)
    private val targetRectangle = RectangleFinder().adjustRectangle(observedRectangle)

    @Test
    fun calcNewPoint() {

        val nearRight = right - 3
        val nearTop = top - 1
        val nearLeft = left - 1

        val originalPoint = Point(nearRight, nearTop)

        val homographyMatrix = HomographyMatrixCalculator.calculate(
            observedRectangle, targetRectangle
        )

        val projectedPoint = PointPositionCalculator.calculatePoint(originalPoint, homographyMatrix)

        assertThat(projectedPoint.x).isGreaterThan(originalPoint.x)
        assertThat(projectedPoint.y).isLessThan(originalPoint.y)


        val originalPointNearTopLeft = Point(nearLeft, nearTop)
        val projectedPointTopLeft =
            PointPositionCalculator.calculatePoint(originalPointNearTopLeft, homographyMatrix)
        assertThat(projectedPointTopLeft.x).isLessThan(originalPointNearTopLeft.x)

        val nearBottom = bottom - 7
        val originalPointNearBottomLeft = Point(nearLeft, nearBottom)

        val projectedPointBottomLeft =
            PointPositionCalculator.calculatePoint(originalPointNearBottomLeft, homographyMatrix)
        assertThat(projectedPointBottomLeft.y).isGreaterThan(originalPointNearBottomLeft.y)

    }

}