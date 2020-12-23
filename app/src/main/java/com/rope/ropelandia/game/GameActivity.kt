package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.rope.ropelandia.PermissionChecker
import com.rope.ropelandia.R
import com.rope.ropelandia.capture.ImageQualityPref
import com.rope.ropelandia.capture.ProgramFactory
import com.rope.ropelandia.rope.RoPE
import com.rope.ropelandia.rope.rope
import kotlinx.android.synthetic.main.main_activity.*
import java.io.File
import java.util.concurrent.ExecutorService

class GameActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture

    private lateinit var photoFile: File
    private lateinit var photoFileOutputOptions: ImageCapture.OutputFileOptions
    private lateinit var imageSavedCallback: ImageSavedCallback
    private val permissionChecker = PermissionChecker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        photoFile = File(filesDir, "topCodes.jpg")
        photoFileOutputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        val imageQualityPref =
            getSharedPreferences(applicationContext.packageName, Context.MODE_PRIVATE).let {
                val defaultValue = ImageQualityPref.MEDIUM.name
                val storedPreference =
                    it.getString(
                        getString(R.string.image_quality_key),
                        defaultValue
                    )
                ImageQualityPref.valueOf(storedPreference!!)
            }

        val imageProcessingConfig = ImageProcessingConfig(photoFile, imageQualityPref)
        imageSavedCallback = ImageSavedCallback(imageProcessingConfig)

        imageSavedCallback.onFoundBlocks { blocks: List<Block> ->
            val blocksSequence = ProgramFactory.findSequence(blocks)

            val program = mutableListOf<RoPE.Action>()

            blocksSequence.map {
                when (it) {
                    is ForwardBlock -> RoPE.Action.FORWARD
                    is BackwardBlock -> RoPE.Action.BACKWARD
                    is LeftBlock -> RoPE.Action.LEFT
                    is RightBlock -> RoPE.Action.RIGHT
                    else -> RoPE.Action.NULL
                }
            }.toCollection(program)

            rope.execute(program)

            updateView(blocksSequence)
        }

        permissionChecker.executeOrRequestPermission(
            this,
            REQUIRED_PERMISSIONS,
            REQUEST_PERMISSIONS_CODE
        ) {
            startCamera()
        }

        rope.onDisconnected {
            returnToPreviousActivity()
        }
        rope.onStartedPressed {
            takePhoto(mat)
        }
        rope.onExecution { actionIndex ->
            val nextAction = actionIndex + 1
            highlight(nextAction)
        }
        rope.onExecutionStarted {
            highlight(0)
        }
        rope.onExecutionFinished {
            mat.hideHighlight()
        }
    }

    private fun highlight(actionIndex: Int) {
        mat.highlight(actionIndex)
    }

    private fun returnToPreviousActivity() {
        Log.i(this.localClassName, "returning to connection activity")
        this.finish()
    }

    private fun updateView(it: List<Block>) {
        mat.blocks = it
        mat.invalidate()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            permissionChecker.executeOrCry(this, REQUIRED_PERMISSIONS) {
                startCamera()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        val cameraProviderFutureListener = Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
        }

        cameraProviderFuture.addListener(
            cameraProviderFutureListener,
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun takePhoto(view: View) {
        imageCapture.takePicture(
            photoFileOutputOptions,
            ContextCompat.getMainExecutor(this),
            imageSavedCallback
        )
    }

    fun togglePreview(view: View) {
        if (previewView.visibility == View.VISIBLE) {
            previewView.visibility = View.INVISIBLE
            mat.setBackgroundColor(Color.BLACK)
        } else {
            previewView.visibility = View.VISIBLE
            mat.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_PERMISSIONS_CODE = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA
        )
    }
}