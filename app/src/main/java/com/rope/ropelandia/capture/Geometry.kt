package com.rope.ropelandia.capture

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

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