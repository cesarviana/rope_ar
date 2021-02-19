package model

import java.util.*

open class Entity {
    val id: UUID = UUID.randomUUID()
    override fun equals(other: Any?): Boolean {
        if(other is Entity)
            return other.id == id
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}