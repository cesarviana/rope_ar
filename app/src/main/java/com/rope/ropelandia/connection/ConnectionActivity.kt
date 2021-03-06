package com.rope.ropelandia.connection

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.rope.connection.fake.RoPEFinderFake
import com.rope.ropelandia.R
import com.rope.ropelandia.app
import com.rope.ropelandia.config.ConfigActivity
import com.rope.ropelandia.databinding.ActivityConnectionBinding
import com.rope.ropelandia.game.GameActivity

class ConnectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectionBinding.inflate(layoutInflater)
        val connectionViewModel = ConnectionViewModel(app)
        binding.connectionStateTextView.text = connectionViewModel.getConnectionState()
        setContentView(binding.root)

        setupRopeFinder()
        setupActivityListeners()
//        findAndConnectRoPE()
    }

    private fun setupRopeFinder() {
        if (app.ropeFinder != null) {
            app.ropeFinder?.activity = this
        } else {
            app.ropeFinder = RoPEFinderFake(this)
            addRoPEFinderListeners()
        }
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        val connectionAllowed = activityResult.resultCode == Activity.RESULT_OK
        if(connectionAllowed)
            app.ropeFinder?.handleConnectionAllowed(connectionAllowed)
        else
            show("Falha ao ativar conexão")
    }

    private fun addRoPEFinderListeners() {
        app.ropeFinder?.onRequestEnableConnection { request ->
            activityResultLauncher.launch(request.intent)
        }
        app.ropeFinder?.onConnectionFailed {
            show("Falha ao conectar")
        }
        app.ropeFinder?.onRoPEFound {
            if(app.rope == null){
                app.rope = it
                setupRoPEListeners()
            }
            if(app.rope?.isConnected() == false){
                app.rope?.connect()
            }
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
        if (ropeFound()) {
            goToGameActivity()
        } else {
            app.rope?.disconnect()
            app.ropeFinder?.findRoPE()
        }
    }

    private fun ropeFound() = app.rope != null && app.rope?.isConnected() == true

    private fun show(message: String) {
        Toast
            .makeText(this, message, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        app.ropeFinder?.handleRequestConnectionPermissionResult(requestCode)
    }

    private fun setupRoPEListeners() {
        app.rope?.onConnected {
            playConnectedSound {
                goToGameActivity()
            }
        }
    }

    private fun playConnectedSound(onPlayed: () -> Unit) {
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