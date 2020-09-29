package com.example.ropelandia

import org.junit.Test

import org.junit.Assert.*

class RectangleFinderTest {
    @Test
    fun findTargetSquare() {

        val topLeft = Point(654.0, 721.0)
        val topRight = Point(1681.0, 721.0)
        val bottomRight = Point(1681.0, 988.0)
        val bottomLeft = Point(646.0, 1016.0)

        val points = listOf(topLeft, topRight, bottomRight, bottomLeft)

        val rectangleFinder = RectangleFinder()

        val rectangle = rectangleFinder.adjustRectangle(points)

        assertEquals(Point(646.0, 721.0), rectangle.topLeft)
        assertEquals(Point(1681.0, 721.0), rectangle.topRight)
        assertEquals(Point(1681.0, 1016.0), rectangle.bottomRight)
        assertEquals(Point(646.0, 1016.0), rectangle.bottomLeft)

    }
}