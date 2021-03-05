package com.rope.connection.ble

import com.rope.connection.RoPE

interface RoPEDisconnectedListener {
    fun disconnected(rope: RoPE)
}

interface RoPEStartPressedListener {
    fun startPressed(rope: RoPE)
}

interface RoPEActionListener {
    fun actionFinished(rope: RoPE)
}

interface RoPEExecutionStartedListener {
    fun executionStarted(rope: RoPE)
}

interface RoPEExecutionFinishedListener {
    fun executionEnded(rope: RoPE)
}