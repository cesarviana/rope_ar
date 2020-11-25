package model

object RoPEFactory {
    fun create() = RoPE(false)
}

data class RoPE(var connected: Boolean)