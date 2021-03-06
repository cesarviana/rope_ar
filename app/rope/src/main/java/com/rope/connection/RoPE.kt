package com.rope.connection

import android.os.Handler
import com.rope.connection.ble.*
import com.rope.program.SequentialProgram

abstract class RoPE(val handler: Handler) {

    object Listeners {
        val onExecutionFinished: MutableList<RoPEExecutionFinishedListener> = mutableListOf()
        val onExecutionStarted: MutableList<RoPEExecutionStartedListener> = mutableListOf()
        val onActionExecution: MutableList<RoPEActionListener> = mutableListOf()
        val onStartPressed: MutableList<RoPEStartPressedListener> = mutableListOf()
        val onMessage: MutableList<(message: String) -> Unit> = mutableListOf()
        var onConnected: (() -> Unit)? = null
        val onDisconnected: MutableList<RoPEDisconnectedListener> = mutableListOf()
    }

    var actionIndex: Int = 0
    var program: Program = SequentialProgram(listOf())

    abstract fun connect()
    abstract fun disconnect()

    abstract fun isConnected(): Boolean
    abstract fun isConnecting(): Boolean
    abstract fun isStopped(): Boolean
    fun isExecuting() = !isStopped()

    abstract fun send(command: String)

    abstract fun execute(program: Program)

    abstract fun onMessage(function: (message: String) -> Unit)

    abstract fun onConnected(function: () -> Unit)
    abstract fun onDisconnected(ropeDisconnectListener: RoPEDisconnectedListener)
    abstract fun onExecutionStarted(executionStartedListener: RoPEExecutionStartedListener)
    abstract fun onExecutionFinished(executionFinishedListener: RoPEExecutionFinishedListener)
    abstract fun onStartedPressed(ropeStartPressedListener: RoPEStartPressedListener)
    abstract fun onActionExecuted(ropeActionListener: RoPEActionListener)

    abstract fun removeDisconnectedListener(listener: RoPEDisconnectedListener)
    abstract fun removeStartPressedListener(listener: RoPEStartPressedListener)
    abstract fun removeActionListener(listener: RoPEActionListener)
    abstract fun removeExecutionStartedListener(listener: RoPEExecutionStartedListener)
    abstract fun removeExecutionFinishedListener(listener: RoPEExecutionFinishedListener)
    abstract fun stop()

    protected fun notifyActionExecuted(action: Action) {
        Listeners.onActionExecution.forEach {
            it.actionExecuted(this, action)
        }
    }

    fun nextActionIs(action: Action): Boolean {
        val nextActionIndex = actionIndex + 1
        val hasNextAction = program.actionList.size > nextActionIndex
        if (hasNextAction) {
            val nextAction = program.actionList[nextActionIndex]
            return nextAction == action
        }
        return false
    }

    protected fun notifyExecutionEnded() {
        Listeners.onExecutionFinished.forEach {
            it.executionEnded(this)
        }
    }

    protected fun notifyExecutionStarted() {
        Listeners.onExecutionStarted.forEach {
            it.executionStarted(this)
        }
    }

    enum class Action {
        BACKWARD {
            override val stringSequence = "b"
        },
        FORWARD {
            override val stringSequence = "f"
        },
        LEFT {
            override val stringSequence = "l"
        },
        RIGHT {
            override val stringSequence = "r"
        },
        EXECUTE {
            override val stringSequence = "e"
        },
        SOUND_OFF {
            override val stringSequence = "s"
        },
        SOUND_ON {
            override val stringSequence = "S"
        },
        NULL {
            override val stringSequence = ""
        },
        ACTIVE_CONNECTION {
            override val stringSequence = "A"
        },
        INACTIVE_CONNECTION {
            override val stringSequence = "a"
        },
        ACTIVE_DIRECTIONAL_BUTTONS {
            override val stringSequence = "X"
        },
        INACTIVE_DIRECTIONAL_BUTTONS {
            override val stringSequence = "x"
        },
        CLEAR_COMMANDS {
            override val stringSequence = "c"
        };

        abstract val stringSequence: String
    }

    interface Program {
        val actionList: List<Action>
    }

    abstract fun sendActions(actionList: List<Action>)
}