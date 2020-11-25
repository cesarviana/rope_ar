package model

import androidx.room.Entity
import java.time.Instant

@Entity
data class Interaction (
    val type: String = "",
    val instant: Instant
) : model.Entity()