package com.example.ropelandia

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class RectangleFinderTest {
    @Test
    fun findTargetSquare() {

        val topLeft = Point(654.0, 721.0)
        val topRight = Point(1681.0, 721.0)
        val bottomRight = Point(1681.0, 988.0)
        val bottomLeft = Point(646.0, 1016.0)

        val points = listOf(topLeft, topRight, bottomRight, bottomLeft)

        val rectangleFinder = RectangleFinder()

        val square = rectangleFinder.calcRectangle(points)

        assertEquals(Point(646.0, 721.0), square.topLeft)
        assertEquals(Point(1681.0, 721.0), square.topRight)
        assertEquals(Point(1681.0, 1016.0), square.bottomRight)
        assertEquals(Point(646.0, 1016.0), square.bottomLeft)

    }
}