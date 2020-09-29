package com.example.ropelandia

import junit.framework.Assert.assertTrue
import org.junit.Assert
import org.junit.Test

class HomographyMatrixFactoryTest {

    private val topLeft = Point(5.0, 5.0)
    private val topRight = Point(15.0, 5.0)
    private val bottomRight = Point(20.0, 20.0)
    private val bottomLeft = Point(0.0, 20.0)

    private val observedRectangle = Rectangle(topLeft, topRight, bottomRight, bottomLeft)
    private val targetRectangle = RectangleFinder().calcRectangle(observedRectangle)

    @Test
    fun findHomographyMatrix() {

        val point = Point(12.0, 4.0)
        val newPoint = HomographyMatrixFactory.create(
            sourceRectangle = observedRectangle,
            targetRectangle = targetRectangle,
            point = point
        )

        assertTrue("Point must go to left", newPoint.x > point.x )
        assertTrue("Point must go to top", newPoint.y < point.y )
    }

}