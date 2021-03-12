package com.rope.ropelandia.connection

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat
import com.rope.connection.ble.RoPEFinderBle
import com.rope.ropelandia.R
import com.rope.ropelandia.app
import com.rope.ropelandia.databinding.ActivityConnectionBinding
import com.rope.ropelandia.game.GameActivity

class ConnectionActivity : AppCompatActivity() {

    private var dataUrl: String? = null
    private lateinit var binding: ActivityConnectionBinding

    private val connectedSound by lazy { MediaPlayer.create(this, R.raw.audio_rope_conectado) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectionBinding.inflate(layoutInflater)
        val connectionViewModel = ConnectionViewModel(app)
        binding.connectionStateTextView.text = connectionViewModel.getConnectionState()
        setContentView(binding.root)

        setupRopeFinder()
        setupActivityListeners()
        findAndConnectRoPE()
        getDataFromDeepLink()
    }

    private fun setupRopeFinder() {
        if (app.ropeFinder != null) {
            app.ropeFinder?.activity = this
        } else {
            val handler = HandlerCompat.createAsync(Looper.getMainLooper())
            app.ropeFinder = RoPEFinderBle(this, handler)
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
        connectedSound.start()
        connectedSound.setOnCompletionListener {
            onPlayed()
        }
    }

    private fun goToGameActivity() {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("dataUrl", dataUrl)
        startActivity(intent)
    }

    private fun getDataFromDeepLink() {
        intent?.let {
            it.data?.let { uri ->
                uri.path?.let { path -> Log.d("CONNECTION_ACTIVITY", path) }
                dataUrl = uri.getQueryParameter("dataUrl")
            }
        }
    }

}