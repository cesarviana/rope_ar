package com.example.ropelandia

import android.content.pm.PackageManager
import android.media.Image
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_test_topcodes.*

class TestTopCodesActivity : AppCompatActivity() {

    private var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_topcodes)
        Camera(this, surfaceView).let {
            camera = it
            setupListeners(it)
            start(it)
        }
    }

    private fun start(camera: Camera) {
        run {
            camera.open()
        }
    }

    private fun setupListeners(camera: Camera) {
        camera.onEachFrameListener = object : Camera.OnEachFrameListener {
            override fun onNewFrame(image: Image) {
//                Scanner().scan(image)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            REQUEST_CAMERA_PERMISSION_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    camera?.let { start(it) }
                }
            }
        }
    }
}