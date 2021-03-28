package com.rope.ropelandia.usecases.perspectiverectangle.domain.repositories

import com.rope.ropelandia.usecases.perspectiverectangle.domain.entities.PerspectiveRectangle

interface PerspectiveRectangleRepository {
    fun storePerspectiveRectangle(perspectiveRectangle: PerspectiveRectangle)
    fun getPerspectiveRectangle() : PerspectiveRectangle
}