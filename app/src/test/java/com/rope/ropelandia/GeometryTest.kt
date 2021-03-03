package com.rope.ropelandia

import com.rope.ropelandia.capture.Circle
import com.rope.ropelandia.capture.Point
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GeometryTest {
    @Test
    fun testDistanceZero(){
        val circle = Circle(Point(10.0,10.0), 10.0)
        val point = Point(10.0,10.0)
        assertThat(circle.distance(point)).isEqualTo(0.0)
    }

    @Test
    fun testDistance(){
        val circle = Circle(Point(10.0,10.0), 10.0)
        val point = Point(11.0,11.0)
        assertThat(circle.distance(point)).isGreaterThan(1.0).isLessThan(2.0)
    }
}