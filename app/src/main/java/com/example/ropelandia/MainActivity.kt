package com.example.ropelandia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText

const val EXTRA_MESSAGE = "com.example.ropelandia.MESSAGE"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun sendMessage(view: View) {
        val editText = findViewById<EditText>(R.id.editText)
        val message = editText.text.toString()
        val intent = Intent(this, DisplayMessageActivity::class.java)
        intent.putExtra(EXTRA_MESSAGE, message)
        startActivity(intent)
    }

    fun goTestTopcodes(view: View) {
        val intent = Intent(this, TestTopCodesActivity::class.java)
        startActivity(intent)
    }
}