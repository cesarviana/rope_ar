package com.rope.connection

import com.rope.connection.ble.*

interface RoPE {
    val actionIndex: Int

    fun connect()
    fun disconnect()

    fun isConnected(): Boolean
    fun isConnecting(): Boolean
    fun isStopped(): Boolean

    fun send(command: String)
    fun execute(vararg actions: Action)

    fun execute(actionList: List<Action>)

    fun onMessage(function: (message: String) -> Unit)

    fun onConnected(function: () -> Unit)
    fun onDisconnected(ropeDisconnectListener: RoPEDisconnectedListener)
    fun onExecutionStarted(executionStartedListener: RoPEExecutionStartedListener)
    fun onExecutionFinished(executionFinishedListener: RoPEExecutionFinishedListener)
    fun onStartedPressed(ropeStartPressedListener: RoPEStartPressedListener)
    fun onActionFinished(ropeActionListener: RoPEActionListener)

    fun removeDisconnectedListener(listener: RoPEDisconnectedListener)
    fun removeStartPressedListener(listener: RoPEStartPressedListener)
    fun removeActionListener(listener: RoPEActionListener)
    fun removeExecutionStartedListener(listener: RoPEExecutionStartedListener)
    fun removeExecutionFinishedListener(listener: RoPEExecutionFinishedListener)

    enum class Action {
        BACKWARD {
            override val stringSequence: String
                get() = "b"
        },
        FORWARD {
            override val stringSequence: String
                get() = "f"
        },
        LEFT {
            override val stringSequence: String
                get() = "l"
        },
        RIGHT {
            override val stringSequence: String
                get() = "r"
        },
        EXECUTE {
            override val stringSequence: String
                get() = "e"
        },
        SOUND_OFF {
            override val stringSequence: String
                get() = "s"
        },
        NULL {
            override val stringSequence: String
                get() = ""
        };

        abstract val stringSequence: String
    }
}