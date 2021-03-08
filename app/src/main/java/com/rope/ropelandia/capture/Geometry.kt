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
    fun height() = top - bottom
    fun width() = right - left
}

data class PerspectiveRectangle(
    val topLeft: Point,
    val topRight: Point,
    val bottomLeft: Point,
    val bottomRight: Point
) {
    fun toRectangle() : Rectangle {
        val top = min(topLeft.y, topRight.y)
        val left = min(topLeft.x, bottomLeft.x)
        val bottom = max(bottomLeft.y, bottomRight.y)
        val right = max(topRight.x, bottomRight.x)
        return Rectangle(left, top, right, bottom)
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

                PerspectiveRectangle(topLeft!!, topRight!!, bottomRight!!, bottomLeft!!)
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