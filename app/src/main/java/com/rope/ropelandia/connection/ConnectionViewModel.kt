package com.rope.ropelandia.connection

import androidx.lifecycle.ViewModel
import com.rope.ropelandia.App

data class ConnectionViewModel(private val app: App) : ViewModel() {

    fun getConnectionState(): String {
        app.rope?.let {
            return when {
                it.isConnected() -> "Conectado"
                it.isConnecting() -> "Conectando..."
                else -> "Desconectado"
            }
        }
        return "Desconectado"
    }

}