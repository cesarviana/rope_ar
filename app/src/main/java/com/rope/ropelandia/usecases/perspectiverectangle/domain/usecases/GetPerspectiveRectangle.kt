package com.rope.ropelandia.usecases.perspectiverectangle.domain.usecases

import com.rope.ropelandia.usecases.perspectiverectangle.domain.repositories.PerspectiveRectangleRepository

class GetPerspectiveRectangle(private val perspectiveRectangleRepository: PerspectiveRectangleRepository) {

    fun execute() = perspectiveRectangleRepository.getPerspectiveRectangle()
}