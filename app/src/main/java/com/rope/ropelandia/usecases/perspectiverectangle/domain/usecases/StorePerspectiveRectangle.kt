package com.rope.ropelandia.usecases.perspectiverectangle.domain.usecases

import com.rope.ropelandia.usecases.perspectiverectangle.domain.entities.PerspectiveRectangle
import com.rope.ropelandia.usecases.perspectiverectangle.domain.repositories.PerspectiveRectangleRepository

class StorePerspectiveRectangle(private val perspectiveRectangleRepository: PerspectiveRectangleRepository) {
    fun execute(perspectiveRectangle: PerspectiveRectangle) =
        perspectiveRectangleRepository.storePerspectiveRectangle(perspectiveRectangle)

}