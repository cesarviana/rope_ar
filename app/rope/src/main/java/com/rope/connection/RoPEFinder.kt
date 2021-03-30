package com.rope.connection

import android.content.Intent

interface RoPEFinder {
    fun findRoPE()
    fun onRequestEnableConnection(onRequestEnableConnection: (EnableConnectionRequest) -> Unit)
    fun handleConnectionAllowed(connectionAllowed: Boolean)
    fun onRoPEFound(function: (rope: RoPE) -> Unit)
    fun handleRequestConnectionPermissionResult(requestCode: Int)
    fun onConnectionFailed(function: (errorCode: Int) -> Unit)
    fun isSearching(): Boolean
    data class EnableConnectionRequest(val intent: Intent, val code: Int)
}