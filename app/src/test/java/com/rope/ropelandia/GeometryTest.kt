package com.rope.ropelandia

import com.rope.ropelandia.capture.Circle
import com.rope.ropelandia.capture.Point
import com.rope.ropelandia.usecases.perspectiverectangle.domain.entities.PerspectiveRectangle
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test

class GeometryTest {
    @Test
    fun testDistanceZero(){
        val circle = Circle(Point(10.0, 10.0), 10.0)
        val point = Point(10.0, 10.0)
        assertThat(circle.distance(point)).isEqualTo(0.0)
    }

    @Test
    fun testDistance(){
        val circle = Circle(Point(10.0, 10.0), 10.0)
        val point = Point(11.0, 11.0)
        assertThat(circle.distance(point)).isGreaterThan(1.0).isLessThan(2.0)
    }

    @Test
    fun testCreatePerspectiveRectangle() {
        val topLeft = Point(774.0, 181.0)
        val topRight = Point(2510.0, 115.0)
        val bottomLeft = Point(776.0, 1007.0)
        val bottomRight = Point(2229.0, 983.0)
        val softly = SoftAssertions()
        val points = listOf(topLeft, topRight, bottomLeft, bottomRight)
        assertRectangle(points, softly)
        assertRectangle(points.shuffled(), softly)
        assertRectangle(points.shuffled(), softly)
        softly.assertAll()
    }

    private fun assertRectangle(points: List<Point>, softly: SoftAssertions) {
        PerspectiveRectangle.createFromPoints(points).toRectangle().let {
            softly.assertThat(it.top).isEqualTo(115.0)
            softly.assertThat(it.left).isEqualTo(774.0)
            softly.assertThat(it.right).isEqualTo(2510.0)
            softly.assertThat(it.bottom).isEqualTo(1007.0)
        }
    }
}