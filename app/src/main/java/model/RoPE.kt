package model

import androidx.fragment.app.Fragment

object RoPEFactory {
    fun create() = RoPE(false)
}

data class RoPE(var connected: Boolean)