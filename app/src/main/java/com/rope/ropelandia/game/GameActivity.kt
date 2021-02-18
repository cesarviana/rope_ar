package com.rope.ropelandia.game

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
import com.rope.connection.RoPE
import com.rope.droideasy.PermissionChecker
import com.rope.ropelandia.R
import com.rope.ropelandia.capture.ImageQuality
import com.rope.ropelandia.capture.ProgramFactory
import com.rope.ropelandia.connection.rope
import com.rope.ropelandia.model.*
import kotlinx.android.synthetic.main.main_activity.*
import java.io.File

class GameActivity : AppCompatActivity() {

    private lateinit var imageCapture: ImageCapture
    private lateinit var photoFileOutputOptions: ImageCapture.OutputFileOptions
    private lateinit var imageSavedCallback: ImageSavedCallback
    private val permissionChecker = PermissionChecker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setupImageSavedCallback()
        setupRopeListeners()
        startCameraOrRequestPermission()
    }

    private fun setupImageSavedCallback() {
        val photoFile = File(filesDir, "topCodes.jpg")
        photoFileOutputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        val imageQualityPref = getImageQualityPreference()
        val imageProcessingConfig = ImageProcessingConfig(photoFile, imageQualityPref)
        imageSavedCallback = ImageSavedCallback(imageProcessingConfig)
        imageSavedCallback.onFoundBlocks { blocks: List<Block> ->
            val program = ProgramFactory.findSequence(blocks)
            mat.program = program
            ropeExecute(program)
        }
    }

    private fun setupRopeListeners() {
        rope?.onDisconnected {
            returnToPreviousActivity()
        }
        rope?.onStartedPressed {
            takePhoto(mat)
        }
        rope?.onActionFinished { actionIndex ->
            val nextAction = actionIndex + 1
            mat.hideHighlight()
            mat.highlight(nextAction)
        }
        rope?.onExecutionStarted {
            mat.highlight(0)
        }
        rope?.onExecutionFinished {
            mat.hideHighlight()
        }
    }

    private fun startCameraOrRequestPermission() {
        permissionChecker.executeOrRequestPermission(
            this,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_CAMERA_PERMISSIONS
        ) {
            startCamera()
        }
    }

    private fun ropeExecute(program: Program) {
        val ropeActions = convertToRoPEActions(program)
        rope?.execute(ropeActions)
    }

    private fun convertToRoPEActions(program: Program): MutableList<RoPE.Action> {
        val ropeActions = mutableListOf<RoPE.Action>()

        program.blocks.map {
            when (it) {
                is ForwardBlock -> RoPE.Action.FORWARD
                is BackwardBlock -> RoPE.Action.BACKWARD
                is LeftBlock -> RoPE.Action.LEFT
                is RightBlock -> RoPE.Action.RIGHT
                else -> RoPE.Action.NULL
            }
        }.toCollection(ropeActions)

        return ropeActions
    }

    private fun getImageQualityPreference(): ImageQuality {
        return getSharedPreferences(
            applicationContext.packageName,
            MODE_PRIVATE
        ).let { sharedPreferences ->
            val defaultImageQuality = ImageQuality.MEDIUM.name
            val storedImageQuality =
                sharedPreferences.getString(
                    getString(R.string.image_quality_key),
                    defaultImageQuality
                )
            ImageQuality.valueOf(storedImageQuality!!)
        }
    }

    private fun returnToPreviousActivity() {
        this.finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSIONS) {
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

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_CAMERA_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA
        )
    }
}