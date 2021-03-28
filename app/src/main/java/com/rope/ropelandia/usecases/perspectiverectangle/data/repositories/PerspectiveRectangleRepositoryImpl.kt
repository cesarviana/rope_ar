package com.rope.ropelandia.usecases.perspectiverectangle.data.repositories

import android.content.SharedPreferences
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.rope.ropelandia.usecases.perspectiverectangle.domain.entities.PerspectiveRectangle
import com.rope.ropelandia.usecases.perspectiverectangle.domain.repositories.PerspectiveRectangleRepository

private const val key = "perspective_rectangle"

class PerspectiveRectangleRepositoryImpl(private val sharedPreferences: SharedPreferences) :
    PerspectiveRectangleRepository {
    private val objectMapper = ObjectMapper()
    override fun storePerspectiveRectangle(perspectiveRectangle: PerspectiveRectangle) {
        sharedPreferences.edit()?.apply {
            val perspectiveRectangleJson = objectMapper.writeValueAsString(perspectiveRectangle)
            putString(key, perspectiveRectangleJson)
            apply()
        }
    }

    override fun getPerspectiveRectangle(): PerspectiveRectangle {
        val type = object : TypeReference<PerspectiveRectangle>() {}
        val string = sharedPreferences.getString(key, "")
        if(string.isNullOrBlank())
            return PerspectiveRectangle()
        return objectMapper.readValue(string, type)
    }
}