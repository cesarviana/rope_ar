package com.rope.connection

import android.app.Activity
import com.rope.connection.ble.RoPEFinderBle

interface RoPEFinder {

    var activity: Activity

    fun findRoPE()
    fun onRequestEnableConnection(onRequestEnableConnection: (RoPEFinderBle.EnableBluetoothRequest) -> Unit)
    fun handleConnectionAllowed(connectionAllowed: Boolean)
    fun onRoPEFound(function: (rope: RoPE) -> Unit)
    fun handleRequestConnectionPermissionResult(requestCode: Int)
    fun onConnectionFailed(function: (errorCode: Int) -> Unit)
}