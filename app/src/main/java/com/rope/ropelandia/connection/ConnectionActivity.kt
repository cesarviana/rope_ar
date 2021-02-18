package com.rope.ropelandia.connection

import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rope.ropelandia.R
import com.rope.ropelandia.config.ConfigActivity
import com.rope.ropelandia.databinding.ActivityConnectionBinding
import com.rope.ropelandia.game.GameActivity

var rope: com.rope.connection.RoPE? = null

class ConnectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectionBinding
    private val ropeFinder = com.rope.connection.RoPEFinder(this)
    private val connectionViewModel = ConnectionViewModel(rope)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectionBinding.inflate(layoutInflater)
        binding.connectionStateTextView.text = connectionViewModel.getConnectionState()
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE

        setupRopeFinder()
        setupActivityListeners()
        findAndConnectRoPE()
    }

    private fun setupRopeFinder() {
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
            rope?.connect()
        }
    }

    private fun setupActivityListeners() {
        binding.connectButton.setOnClickListener {
            findAndConnectRoPE()
        }
        binding.configButton.setOnClickListener {
            val intent = Intent(this, ConfigActivity::class.java)
            startActivity(intent)
        }
    }

    private fun findAndConnectRoPE() {
        if (ropeFound())
            goToGameActivity()
        else
            ropeFinder.findRoPE()
    }

    private fun ropeFound() = rope != null && rope?.isConnected() == true

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
        rope?.onConnected {
            playConnectedSound {
                goToGameActivity()
            }
        }
    }

    private fun playConnectedSound(onPlayed: ()->Unit) {
        val mediaPlayer = MediaPlayer.create(this, R.raw.audio_rope_conectado)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            onPlayed()
        }
    }

    private fun goToGameActivity() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

}