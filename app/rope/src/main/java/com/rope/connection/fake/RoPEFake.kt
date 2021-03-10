package com.rope.connection.fake

import android.os.Handler
import android.util.Log
import com.rope.connection.RoPE
import com.rope.connection.ble.*
import java.util.concurrent.Executors

class RoPEFake(handler: Handler) : RoPE(handler) {

    private var connected = false

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

    private var executor = Executors.newSingleThreadExecutor()

    override fun execute(program: Program) {
        this.program = program

        if(executor.isShutdown)
            executor = Executors.newSingleThreadExecutor()

        actionIndex = 0

        executor.execute {
            try {
                handler.post {
                    notifyExecutionStarted()
                }
                program.actionList.forEach { _ ->
                    Thread.sleep(1000)
                    handler.post {
                        notifyActionExecuted()
                        actionIndex++
                    }
                }
                notifyExecutionEnded()
            } catch (e: InterruptedException) {
                Log.d("ROPE_FAKE","execution canceled")
            }
        }

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

    override fun onActionExecuted(ropeActionListener: RoPEActionListener) {
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

    override fun stop() {
        //executor.shutdownNow()
    }
}