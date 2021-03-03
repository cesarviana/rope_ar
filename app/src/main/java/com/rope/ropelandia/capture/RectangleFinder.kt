package com.rope.ropelandia.capture

class RectangleFinder {
    fun adjustRectangle(points: List<Point>): Rectangle {

        require(points.size == 4) {
            "4 points must be informed"
        }

        val xs = points.map { it.x }
        val ys = points.map { it.y }

        val topLeft = Point(xs.minOrNull()!!, ys.minOrNull()!!)
        val topRight = Point(xs.maxOrNull()!!, ys.minOrNull()!!)
        val bottomRight = Point(xs.maxOrNull()!!, ys.maxOrNull()!!)
        val bottomLeft = Point(xs.minOrNull()!!, ys.maxOrNull()!!)

        return Rectangle(topLeft, topRight, bottomRight, bottomLeft)
    }

    fun adjustRectangle(rectangle: Rectangle): Rectangle {
        val points = listOf(
            rectangle.topLeft,
            rectangle.topRight,
            rectangle.bottomRight,
            rectangle.bottomLeft
        )
        return adjustRectangle(
            points
        )
    }
}


