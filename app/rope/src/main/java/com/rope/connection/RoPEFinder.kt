package com.rope.connection

import android.app.Activity
import com.rope.connection.ble.RoPEFinderBle

interface RoPEFinder {

    var activity: Activity

    fun findRoPE()
    fun onRequestEnableConnection(onRequestEnableConnection: (RoPEFinderBle.EnableBluetoothRequest) -> Unit)
    fun handleRequestEnableConnectionResult(requestCode: Int, resultCode: Int)
    fun onEnableConnectionRefused(function: (resultCode: Int) -> Unit)
    fun onRoPEFound(function: (rope: RoPE) -> Unit)
    fun handleRequestConnectionPermissionResult(requestCode: Int)
    fun onConnectionFailed(function: (errorCode: Int) -> Unit)
}