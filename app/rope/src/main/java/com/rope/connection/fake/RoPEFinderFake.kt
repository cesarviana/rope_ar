package com.rope.connection.fake

import android.app.Activity
import com.rope.connection.RoPE
import com.rope.connection.RoPEFinder
import com.rope.connection.ble.RoPEFinderBle

class RoPEFinderFake(override var activity: Activity) : RoPEFinder {

    private lateinit var onConnectionFailedListener: (errorCode: Int) -> Unit
    private lateinit var onRoPEFoundListener: (rope: RoPE) -> Unit
    private val rope = RoPEFake()

    override fun findRoPE() {
        onRoPEFoundListener(rope)
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