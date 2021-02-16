package com.rope.ropelandia.connection

import androidx.lifecycle.ViewModel
import com.rope.connection.RoPE

data class ConnectionViewModel(private val rope: RoPE?) : ViewModel() {

    fun getConnectionState(): String {
        rope?.let {
            return when {
                rope.isConnected() -> "Conectado"
                rope.isConnecting() -> "Conectando..."
                else -> "Desconectado"
            }
        }
        return "Desconectado"
    }

}