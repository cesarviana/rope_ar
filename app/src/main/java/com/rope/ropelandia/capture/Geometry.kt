package com.rope.ropelandia.capture

import kotlin.math.*

data class Point(val x: Double, val y: Double)

/**
 * Can be a perspective rectangle, so the sides can have different widths.
 */
data class Rectangle(
    val left: Double,
    val top: Double,
    val right: Double,
    val bottom: Double
) {
    fun height() = bottom - top
    fun width() = right - left
}

data class PerspectiveRectangle(
    val topLeft: Point,
    val topRight: Point,
    val bottomLeft: Point,
    val bottomRight: Point
) {

    init {
        require(bottomRight.x >= bottomLeft.x) { "Bottom right is less than bottom left!" }
        require(topRight.x >= topLeft.x) { "Top right is less than top left!" }
        require(bottomRight.y >= topRight.y) { "Bottom right is less than top right!"}
        require(bottomLeft.y >= topLeft.y) { "Bottom left is less than top left!"}
    }

    fun toRectangle() : Rectangle {
        val top = min(topLeft.y, topRight.y)
        val left = min(topLeft.x, bottomLeft.x)
        val bottom = max(bottomLeft.y, bottomRight.y)
        val right = max(topRight.x, bottomRight.x)
        return Rectangle(left, top, right, bottom)
    }

    fun bottomWidth() = bottomRight.x - bottomLeft.x
    fun resize(proportion: Double): PerspectiveRectangle {

        val topLeft = Point(this.topLeft.x * proportion, this.topLeft.y * proportion)
        val topRight = Point(this.topRight.x * proportion, this.topRight.y * proportion)
        val bottomLeft = Point(this.bottomLeft.x * proportion, this.bottomLeft.y * proportion)
        val bottomRight = Point(this.bottomRight.x * proportion, this.bottomRight.y * proportion)

        return PerspectiveRectangle(
            topLeft, topRight, bottomLeft, bottomRight
        )
    }

    companion object {
        fun createFromPoints(points: List<Point>): PerspectiveRectangle {
            require(points.size == 4) {
                "4 points must be informed"
            }

            return points.sortedBy { it.x }.let { sortedPoints ->
                val leftPoints = sortedPoints.subList(0, 2)
                val rightPoints = sortedPoints.subList(2, 4)

                val topLeft = leftPoints.minByOrNull { it.y }
                val bottomLeft = leftPoints.maxByOrNull { it.y }
                val topRight = rightPoints.minByOrNull { it.y }
                val bottomRight = rightPoints.maxByOrNull { it.y }

                PerspectiveRectangle(topLeft!!, topRight!!, bottomLeft!!, bottomRight!!)
            }
        }
    }
}

data class Circle(val center: Point, val radius: Double) {
    fun intersect(point: Point): Boolean {
        val xDistance = abs(point.x - center.x)
        val yDistance = abs(point.y - center.y)
        val intersectX = xDistance < radius
        val intersectY = yDistance < radius
        return intersectX && intersectY
    }

    fun distance(point: Point): Double {
        val diffX = point.x - center.x
        val diffY = point.y - center.y
        val powX = diffX.pow(2)
        val powY = diffY.pow(2)
        return sqrt(powX + powY)
    }
}