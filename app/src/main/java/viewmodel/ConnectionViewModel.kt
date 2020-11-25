package viewmodel

import model.RoPE
import model.RoPEFactory
import model.Task

data class ConnectionViewModel(
    val rope: RoPE = RoPEFactory.create()
) {
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

    fun connectionState(): String {
        if (rope.connected) {
            return "Conectado"
        }
        return "Desconectado"
    }

    fun nextTask() {
        taskIndex++
    }

    fun previousTask() {
        taskIndex--
    }
}