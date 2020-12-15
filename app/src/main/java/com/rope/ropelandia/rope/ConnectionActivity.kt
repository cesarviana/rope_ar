package com.rope.ropelandia.rope

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.rope.ropelandia.R
import com.rope.ropelandia.databinding.ActivityConnectionBinding
import com.rope.ropelandia.log.Logger

class ConnectionActivity : AppCompatActivity() {

    private lateinit var rope: RoPE
    private lateinit var ropeFinder: RoPEFinder

    private lateinit var binding: ActivityConnectionBinding

    private lateinit var logger: Logger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connection)

        ropeFinder = RoPEFinder(this)

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

        logger = Logger(this)

        ropeFinder.findRoPE()
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
        rope.onDisconnected {
            show("RoPE desconectou")
        }
        rope.onConnected {
            //show("RoPE Conectou")
            rope.go(RoPE.Action.BACKWARD)
        }
        rope.onMessage {
            show(it)
        }
    }

}