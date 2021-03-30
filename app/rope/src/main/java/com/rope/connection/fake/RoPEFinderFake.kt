package com.rope.connection.fake

import android.os.Handler
import com.rope.connection.RoPE
import com.rope.connection.RoPEFinder
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class RoPEFinderFake(handler: Handler) : RoPEFinder {

    private lateinit var onConnectionFailedListener: (errorCode: Int) -> Unit
    private lateinit var onRoPEFoundListener: (rope: RoPE) -> Unit
    private val rope = RoPEFake(handler,)
    private var scanning = false
    override fun findRoPE() {
        scanning = true
        val search = thread(start = false) {
            scanning = false
            onRoPEFoundListener(rope)
        }
        Executors.newSingleThreadScheduledExecutor().schedule(search, 10, TimeUnit.SECONDS)
    }

    override fun onRequestEnableConnection(onRequestEnableConnection: (RoPEFinder.EnableConnectionRequest) -> Unit) {
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

    override fun isSearching() = scanning

}