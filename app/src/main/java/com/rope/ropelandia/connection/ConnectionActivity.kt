package com.rope.ropelandia.connection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat
import com.rope.connection.ble.RoPEFinderBle
import com.rope.ropelandia.R
import com.rope.ropelandia.app
import com.rope.ropelandia.game.GameActivity
import com.rope.ropelandia.game.Sounds
import kotlinx.android.synthetic.main.activity_connection.*

class ConnectionActivity : AppCompatActivity() {

    private var dataUrl: String? = null

    private val animation by lazy {
        TranslateAnimation(-50f, 50f, 0f, 0f)
            .apply {
                duration = 2000
                fillAfter = true
                repeatCount = 10
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        setupRopeFinder()
        setupActivityListeners()
        findAndConnectRoPE()
        getDataFromDeepLink()
    }

    override fun onResume() {
        super.onResume()
        Sounds.initialize(this)
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
        if (connectionAllowed)
            app.ropeFinder?.handleConnectionAllowed(connectionAllowed)
        else
            show("Falha ao ativar conexão")
    }

    private fun addRoPEFinderListeners() {
        app.ropeFinder?.onRequestEnableConnection { request ->
            activityResultLauncher.launch(request.intent)
        }
        app.ropeFinder?.onConnectionFailed {
            Sounds.play(Sounds.connectionFailed)
            show("Falha ao conectar")
        }
        app.ropeFinder?.onRoPEFound {
            runOnUiThread {
                showLoader(false)
                if (app.rope == null) {
                    app.rope = it
                    setupRoPEListeners()
                }
                if (app.rope?.isConnected() == false) {
                    app.rope?.connect()
                }
            }
        }
    }

    private fun setupActivityListeners() {
        loup.setOnClickListener {
            findAndConnectRoPE()
        }
    }

    private fun findAndConnectRoPE() {
        if (ropeFound()) {
            goToGameActivity()
        } else {
            Sounds.play(Sounds.connectingSound)
            showLoader(true)
            app.rope?.disconnect()
            app.ropeFinder?.findRoPE()
        }
    }

    private fun showLoader(searchingRoPE: Boolean) {
        if (searchingRoPE) {
            loup.startAnimation(animation)
        } else {
            loup.animation.cancel()
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
        Sounds.play(Sounds.connectedSound) {
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