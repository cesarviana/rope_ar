package com.rope.ropelandia

import android.app.Application
import com.rope.connection.RoPE
import com.rope.connection.RoPEFinder

lateinit var app: App

class App : Application() {

    var ropeFinder: RoPEFinder? = null
    var rope: RoPE? = null

    override fun onCreate() {
        super.onCreate()
        app = this
    }

}