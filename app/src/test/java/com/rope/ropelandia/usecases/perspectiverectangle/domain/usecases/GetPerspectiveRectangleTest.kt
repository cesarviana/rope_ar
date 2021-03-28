package com.rope.ropelandia.usecases.perspectiverectangle.domain.usecases

import com.rope.ropelandia.capture.Point
import com.rope.ropelandia.usecases.perspectiverectangle.domain.entities.PerspectiveRectangle
import com.rope.ropelandia.usecases.perspectiverectangle.domain.repositories.PerspectiveRectangleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class GetPerspectiveRectangleTest {

    private val repository = Mockito.mock(PerspectiveRectangleRepository::class.java)

    private val rectangle = PerspectiveRectangle.createFromPoints(
        listOf(
            Point(0.0, 0.0),
            Point(1.0, 0.0),
            Point(0.0, 1.0),
            Point(1.0, 1.0)
        )
    )

    private val useCase = GetPerspectiveRectangle(repository)

    @Test
    fun test() {
        `when`(repository.getPerspectiveRectangle()).thenReturn(rectangle)
        val rectangle = useCase.execute()
        assertThat(rectangle.topLeft.x).isEqualTo(0.0)
    }

}