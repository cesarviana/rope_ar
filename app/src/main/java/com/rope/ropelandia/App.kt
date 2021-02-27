package com.rope.ropelandia

import android.app.Application
import com.rope.connection.RoPE

lateinit var app: App

class App : Application() {
    var rope: RoPE? = null

    override fun onCreate() {
        super.onCreate()
        app = this
    }

}