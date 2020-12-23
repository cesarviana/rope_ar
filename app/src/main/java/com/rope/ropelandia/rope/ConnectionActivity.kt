package com.rope.ropelandia.rope

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rope.ropelandia.R
import com.rope.ropelandia.config.ConfigActivity
import com.rope.ropelandia.game.GameActivity
import com.rope.ropelandia.log.Logger

lateinit var rope: RoPE

class ConnectionActivity : AppCompatActivity() {

    private lateinit var logger: Logger
    private val ropeFinder = RoPEFinder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE

        Log.i(this.localClassName, "Starting connection activity")
        ropeFinder.initialize(this)

        ropeFinder.onRequestEnableConnection { request ->
            startActivityForResult(request.intent, request.code)
        }
        ropeFinder.onEnableConnectionRefused {
            show("Falha ao ativar conexão")
        }
        ropeFinder.onConnectionFailed {
            show("Falha ao conectar")
        }
        ropeFinder.onRoPEFound {
            rope = it
            setupRoPEListeners()
            rope.connect()
        }

        findViewById<Button>(R.id.connectButton).setOnClickListener {
            ropeFinder.findRoPE()
        }

        findViewById<Button>(R.id.configButton).setOnClickListener {
            val intent = Intent(this, ConfigActivity::class.java)
            startActivity(intent)
        }

        logger = Logger(this)
    }

    private fun show(message: String) {
        Toast
            .makeText(this, message, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ropeFinder.handleRequestEnableConnectionResult(requestCode, resultCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        ropeFinder.handleRequestConnectionPermissionResult(requestCode)
    }

    private fun setupRoPEListeners() {
        rope.onConnected {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }

}