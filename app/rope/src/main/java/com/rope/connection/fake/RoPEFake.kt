package com.rope.connection.fake

import com.rope.connection.RoPE
import com.rope.connection.ble.*

class RoPEFake : RoPE {

    private var connected = false

    private object Listeners {
        val onExecutionFinished: MutableList<RoPEExecutionFinishedListener> = mutableListOf()
        val onExecutionStarted: MutableList<RoPEExecutionStartedListener> = mutableListOf()
        val onActionExecution: MutableList<RoPEActionListener> = mutableListOf()
        val onStartPressed: MutableList<RoPEStartPressedListener> = mutableListOf()
        val onMessage: MutableList<(message: String) -> Unit> = mutableListOf()
        var onConnected: (() -> Unit)? = null
        val onDisconnected: MutableList<RoPEDisconnectedListener> = mutableListOf()
    }

    override var actionIndex: Int = 0

    override fun connect() {
        connected = true
        Listeners.onConnected?.invoke()
    }

    override fun disconnect() {
        Listeners.onDisconnected.forEach { it.disconnected(this) }
    }

    override fun isConnected() = connected

    override fun isConnecting(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isStopped(): Boolean {
        TODO("Not yet implemented")
    }

    override fun send(command: String) {
        TODO("Not yet implemented")
    }

    override fun execute(vararg actions: RoPE.Action) {
        TODO("Not yet implemented")
    }

    override fun execute(actionList: List<RoPE.Action>) {
        TODO("Not yet implemented")
    }

    override fun onMessage(function: (message: String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun onConnected(function: () -> Unit) {
        Listeners.onConnected = function
    }

    override fun onDisconnected(ropeDisconnectListener: RoPEDisconnectedListener) {
        Listeners.onDisconnected.add(ropeDisconnectListener)
    }

    override fun onExecutionStarted(executionStartedListener: RoPEExecutionStartedListener) {
        Listeners.onExecutionStarted.add(executionStartedListener)
    }

    override fun onExecutionFinished(executionFinishedListener: RoPEExecutionFinishedListener) {
        Listeners.onExecutionFinished.add(executionFinishedListener)
    }

    override fun onStartedPressed(ropeStartPressedListener: RoPEStartPressedListener) {
        Listeners.onStartPressed.add(ropeStartPressedListener)
    }

    override fun onActionFinished(ropeActionListener: RoPEActionListener) {
        Listeners.onActionExecution.add(ropeActionListener)
    }

    override fun removeDisconnectedListener(listener: RoPEDisconnectedListener) {
        Listeners.onDisconnected.remove(listener)
    }

    override fun removeStartPressedListener(listener: RoPEStartPressedListener) {
        Listeners.onStartPressed.remove(listener)
    }

    override fun removeActionListener(listener: RoPEActionListener) {
        Listeners.onActionExecution.remove(listener)
    }

    override fun removeExecutionStartedListener(listener: RoPEExecutionStartedListener) {
        Listeners.onExecutionStarted.remove(listener)
    }

    override fun removeExecutionFinishedListener(listener: RoPEExecutionFinishedListener) {
        Listeners.onExecutionFinished.remove(listener)
    }
}