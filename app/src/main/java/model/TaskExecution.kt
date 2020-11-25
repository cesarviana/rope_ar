package model

import java.time.Instant
import java.util.*

data class TaskExecution(
    val startTime: Instant,
    val endTime: Instant,
    val description: String,
    val userId: UUID,
    val taskId: UUID
): model.Entity()