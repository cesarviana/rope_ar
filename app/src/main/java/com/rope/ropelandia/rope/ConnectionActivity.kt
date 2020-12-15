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
            Toast
                .makeText(this, "Falha ao ativar conexão", Toast.LENGTH_SHORT)
                .show()
        }
        ropeFinder.onRoPEFound {
            this.rope = it
        }

        findViewById<Button>(R.id.connectButton).setOnClickListener {
            ropeFinder.findRoPE()
        }

        logger = Logger(this)

        ropeFinder.findRoPE()
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

}