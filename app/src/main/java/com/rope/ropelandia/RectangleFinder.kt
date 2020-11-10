package com.rope.ropelandia

class RectangleFinder {
    fun adjustRectangle(points: List<Point>): Rectangle {

        require(points.size == 4) {
            "4 points must be informed"
        }

        val xs = points.map { it.x }
        val ys = points.map { it.y }

        val topLeft = Point(xs.min()!!, ys.min()!!)
        val topRight = Point(xs.max()!!, ys.min()!!)
        val bottomRight = Point(xs.max()!!, ys.max()!!)
        val bottomLeft = Point(xs.min()!!, ys.max()!!)

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

data class Point(val x: Double, val y: Double)
data class Rectangle(
    val topLeft: Point,
    val topRight: Point,
    val bottomRight: Point,
    val bottomLeft: Point
) {
    fun height(): Double {
        return kotlin.math.max(leftHeight(), rightHeight())
    }

    private fun leftHeight() = bottomLeft.y - topLeft.y
    private fun rightHeight() = bottomRight.y - topRight.y

    fun width(): Double {
        return kotlin.math.max(topWidth(), bottomWidth())
    }

    private fun topWidth() = topRight.x - topLeft.x
    private fun bottomWidth() = bottomRight.x - bottomLeft.x
}
