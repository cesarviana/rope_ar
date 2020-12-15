package viewmodel

import com.rope.ropelandia.rope.RoPE
import model.Task

data class ConnectionViewModel(val rope: RoPE) {
    private val finalFreeTask = Task("Tarefa livre final")

    var tasks = listOf<Task>()

    private var taskIndex = 0

    val task: Task
        get() {
            val invalidIndex = taskIndex < 0 || taskIndex >= tasks.size
            if (invalidIndex) {
                return finalFreeTask
            }
            return tasks[taskIndex]
        }

    fun connectionState() = "Implement"

    fun nextTask() {
        taskIndex++
    }

    fun previousTask() {
        taskIndex--
    }
}