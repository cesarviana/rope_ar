package com.rope.connection.fake

import android.app.Activity
import android.os.Handler
import com.rope.connection.RoPE
import com.rope.connection.RoPEFinder
import com.rope.connection.ble.RoPEFinderBle
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class RoPEFinderFake(override var activity: Activity, handler: Handler) : RoPEFinder {

    private lateinit var onConnectionFailedListener: (errorCode: Int) -> Unit
    private lateinit var onRoPEFoundListener: (rope: RoPE) -> Unit
    private val rope = RoPEFake(handler,)

    override fun findRoPE() {
        val search = thread(start = false) {
            onRoPEFoundListener(rope)
        }
        Executors.newSingleThreadScheduledExecutor().schedule(search, 10, TimeUnit.SECONDS)
    }

    override fun onRequestEnableConnection(onRequestEnableConnection: (RoPEFinderBle.EnableBluetoothRequest) -> Unit) {
    }

    override fun handleConnectionAllowed(connectionAllowed: Boolean) {
    }

    override fun onRoPEFound(function: (rope: RoPE) -> Unit) {
        this.onRoPEFoundListener = function
    }

    override fun handleRequestConnectionPermissionResult(requestCode: Int) {
    }

    override fun onConnectionFailed(function: (errorCode: Int) -> Unit) {
        this.onConnectionFailedListener = function
    }
}