package com.rope.ropelandia.game

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.rope.connection.*
import com.rope.droideasy.PermissionChecker
import com.rope.ropelandia.R
import com.rope.ropelandia.app
import com.rope.ropelandia.capture.ImageQuality
import com.rope.ropelandia.capture.ProgramFactory
import com.rope.ropelandia.model.*
import kotlinx.android.synthetic.main.main_activity.*
import java.io.File

class GameActivity : AppCompatActivity(),
    RoPEDisconnectedListener,
    RoPEStartPressedListener,
    RoPEActionListener,
    RoPEExecutionStartedListener,
    RoPEExecutionFinishedListener {

    private var startRequired: Boolean = false
    private lateinit var imageCapture: ImageCapture
    private lateinit var photoFileOutputOptions: ImageCapture.OutputFileOptions
    private lateinit var imageSavedCallback: ImageSavedCallback
    private val permissionChecker = PermissionChecker()
    private lateinit var levels: List<Level>
    private var levelIndex = 0
    private var program: Program = Program(listOf())

    private val processHandler = ProcessHandler(Looper.getMainLooper()) {
        takePhoto()
    }

    class ProcessHandler(looper: Looper, val takePhotoFun: () -> Unit) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == BLOCKS_ANALYSED || msg.what == EXECUTION_FINISHED) {
                scheduleTakePhoto()
            }
        }

        fun scheduleTakePhoto() {
            post {
                takePhotoFun()
            }
        }

        companion object {
            const val BLOCKS_ANALYSED = 0
            const val EXECUTION_FINISHED = 1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setupImageSavedCallback()
        setupRopeListeners()
        startCameraOrRequestPermission()
        levels = LevelLoader.load(applicationContext)
        startLevel(getLevel(levelIndex))
        processHandler.scheduleTakePhoto()
    }

    override fun onStop() {
        super.onStop()
        app.rope?.removeDisconnectedListener(this)
        app.rope?.removeStartPressedListener(this)
        app.rope?.removeActionListener(this)
        app.rope?.removeExecutionStartedListener(this)
        app.rope?.removeExecutionFinishedListener(this)
    }

    private fun setupImageSavedCallback() {
        val photoFile = File(filesDir, "topCodes.jpg")
        photoFileOutputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        val imageQualityPref = getImageQualityPreference()
        val imageProcessingConfig = ImageProcessingConfig(photoFile, imageQualityPref)
        imageSavedCallback = ImageSavedCallback(imageProcessingConfig)
        imageSavedCallback.onFoundBlocks { blocks: List<Block> ->
            Log.d("GAME_VIEW", "Blocks found: ${blocks.size}")
            program = ProgramFactory.findSequence(blocks)
            updateViewWithProgram()
            if(startRequired) {
                startRequired = false
                ropeExecute(program)
            }
            processHandler.sendEmptyMessage(ProcessHandler.BLOCKS_ANALYSED)
        }
    }

    private fun updateViewWithProgram() {
        if(program.blocks.isEmpty())
            return
        val programBlocksViews = program.blocks.map { block ->
            BlockView(this).apply {
                bounds = Rect(
                    block.left.toInt(),
                    block.top.toInt(),
                    block.right.toInt(),
                    block.bottom.toInt()
                )
                angle = block.angle
            }
        }
        gameView.blocksViews = programBlocksViews
        gameView.invalidate()
    }

    private fun setupRopeListeners() {
        app.rope?.onDisconnected(this)
        app.rope?.onStartedPressed(this)
        app.rope?.onActionFinished(this)
        app.rope?.onExecutionStarted(this)
        app.rope?.onExecutionFinished(this)
    }

    override fun disconnected(rope: RoPE) {
        returnToPreviousActivity()
    }

    override fun startPressed(rope: RoPE) {
        startRequired = true
    }

    override fun actionFinished(rope: RoPE) {
        val nextAction = rope.actionIndex + 1
        gameView.hideHighlight()
        gameView.setExecuting(nextAction)
        matView.invalidate()
    }

    override fun executionStarted(rope: RoPE) {
        gameView.setExecuting(0)
    }

    override fun executionEnded(rope: RoPE) {
        gameView.hideHighlight()
        processHandler.sendEmptyMessage(ProcessHandler.EXECUTION_FINISHED)
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
        app.rope?.execute(ropeActions)
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

    private fun takePhoto() {
        if(imageCapture == null)
            return
        imageCapture.takePicture(
            photoFileOutputOptions,
            ContextCompat.getMainExecutor(this),
            imageSavedCallback
        )
    }

    fun toggleCameraPreview(view: View) {
        if (previewView.visibility == View.VISIBLE) {
            previewView.visibility = View.INVISIBLE
        } else {
            previewView.visibility = View.VISIBLE
        }
    }

    private fun getLevel(taskIndex: Int): Level {
        if (levels.size > taskIndex) {
            return levels[taskIndex]
        }
        return Level()
    }

    private fun startLevel(level: Level) {
        matView.mat = level.mat
        gameView.matView = matView
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_CAMERA_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA
        )
    }
}