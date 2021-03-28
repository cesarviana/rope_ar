package com.rope.ropelandia.capture

import kotlin.math.*

data class Point(val x: Double = 0.0, val y: Double = 0.0)

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