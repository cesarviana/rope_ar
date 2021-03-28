package com.rope.ropelandia.usecases.perspectiverectangle.data.repositories

import android.content.SharedPreferences
import com.rope.ropelandia.capture.Point
import com.rope.ropelandia.usecases.perspectiverectangle.domain.entities.PerspectiveRectangle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*

class PerspectiveRectangleRepositoryImplTest {

    private val sharedPreferences = mock(SharedPreferences::class.java)
    private val perspectiveRectangleRepositoryImpl =
        PerspectiveRectangleRepositoryImpl(sharedPreferences)

    private val rectangle = PerspectiveRectangle.createFromPoints(
        listOf(
            Point(0.0, 0.0),
            Point(1.0, 0.0),
            Point(0.0, 1.0),
            Point(1.0, 1.0)
        )
    )

    @Test
    fun testStorePerspectiveRectangle() {
        perspectiveRectangleRepositoryImpl.storePerspectiveRectangle(rectangle)
        verify(sharedPreferences, times(1)).edit()
    }

    @Test
    fun testGetPositionBlocks() {
        `when`(sharedPreferences.getString("perspective_rectangle", "")).thenReturn(
            """
                {
                    "topLeft": {"x":0,"y":0},
                    "topRight": {"x":1,"y":0},
                    "bottomLeft": {"x":0,"y":1},
                    "bottomRight": {"x":1,"y":1}
                }
            """.trimIndent()
        )
        val perspectiveRectangle = perspectiveRectangleRepositoryImpl.getPerspectiveRectangle()
        assertThat(perspectiveRectangle.topLeft.x).isEqualTo(0.0)
        verify(sharedPreferences, times(1)).getString("perspective_rectangle", "")
    }
}