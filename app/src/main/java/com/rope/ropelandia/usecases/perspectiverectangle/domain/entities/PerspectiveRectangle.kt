package com.rope.ropelandia.usecases.perspectiverectangle.domain.entities

import kotlin.math.*
import com.rope.ropelandia.capture.Point
import com.rope.ropelandia.capture.Rectangle

data class PerspectiveRectangle(
    val topLeft: Point = Point(),
    val topRight: Point = Point(),
    val bottomLeft: Point = Point(),
    val bottomRight: Point = Point()
) {

    init {
        require(bottomRight.x >= bottomLeft.x) { "Bottom right is less than bottom left!" }
        require(topRight.x >= topLeft.x) { "Top right is less than top left!" }
        require(bottomRight.y >= topRight.y) { "Bottom right is less than top right!" }
        require(bottomLeft.y >= topLeft.y) { "Bottom left is less than top left!" }
    }

    fun toRectangle(): Rectangle {
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