package com.example.ropelandia

class RectangleFinder {
    fun calcRectangle(points: List<Point>): Rectangle {

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
}

data class Point(val x: Double, val y: Double)
data class Rectangle(
    val topLeft: Point,
    val topRight: Point,
    val bottomRight: Point,
    val bottomLeft: Point
)
