package com.rope.ropelandia.usecases.perspectiverectangle.domain.usecases

import com.rope.ropelandia.capture.Point
import com.rope.ropelandia.capture.Rectangle
import com.rope.ropelandia.usecases.perspectiverectangle.domain.entities.PerspectiveRectangle
import com.rope.ropelandia.usecases.perspectiverectangle.domain.repositories.PerspectiveRectangleRepository
import org.junit.Test
import org.mockito.Mockito.*

class StorePerspectiveRectangleUseCaseTest {

    private val repository = mock(PerspectiveRectangleRepository::class.java)

    private val points = listOf(
        Point(0.0, 0.0),
        Point(1.0, 0.0),
        Point(0.0, 1.0),
        Point(1.0, 1.0)
    )

    private val useCase: StorePerspectiveRectangle = StorePerspectiveRectangle(repository)

    @Test
    fun test() {
        val perspectiveRectangle = PerspectiveRectangle.createFromPoints(points)
        useCase.execute(perspectiveRectangle)
        verify(repository, times(1))
            .storePerspectiveRectangle(anyObject())
    }

}