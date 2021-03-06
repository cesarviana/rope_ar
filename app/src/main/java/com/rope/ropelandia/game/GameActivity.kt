package com.rope.ropelandia.game

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.impl.ImageCaptureConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat
import com.rope.connection.RoPE
import com.rope.connection.ble.*
import com.rope.droideasy.PermissionChecker
import com.rope.ropelandia.R
import com.rope.ropelandia.app
import com.rope.ropelandia.capture.BitmapToBlocksConverter
import com.rope.ropelandia.capture.ImageToBitmapConverter
import com.rope.ropelandia.capture.ProgramFactory
import com.rope.ropelandia.model.*
import kotlinx.android.synthetic.main.main_activity.*
import java.util.concurrent.Executors

class GameActivity : AppCompatActivity(),
    RoPEDisconnectedListener,
    RoPEStartPressedListener,
    RoPEActionListener,
    RoPEExecutionStartedListener,
    RoPEExecutionFinishedListener {

    private val permissionChecker by lazy { PermissionChecker() }
    private val levels: List<Level> by lazy { LevelLoader.load(applicationContext) }
    private val imageToBitmapConverter by lazy { ImageToBitmapConverter(applicationContext) }
    private val bitmapToBlocksConverter by lazy { BitmapToBlocksConverter() }
    private lateinit var imageCapture: ImageCapture
    private var levelIndex = 0
    private var program: Program = Program(listOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setupRopeListeners()
        System.loadLibrary("native-lib")
        startCameraOrRequestPermission()
        startLevel(getLevel(levelIndex))
    }

    override fun onStop() {
        super.onStop()
        app.rope?.removeDisconnectedListener(this)
        app.rope?.removeStartPressedListener(this)
        app.rope?.removeActionListener(this)
        app.rope?.removeExecutionStartedListener(this)
        app.rope?.removeExecutionFinishedListener(this)
    }

    private fun updateViewWithProgram() {
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
        ropeExecute(program)
    }

    override fun actionFinished(rope: RoPE) {
        val nextAction = rope.actionIndex + 1
        runOnUiThread {
            gameView.hideHighlight()
            gameView.setExecuting(nextAction)
            matView.invalidate()
        }
    }

    override fun executionStarted(rope: RoPE) {
        gameView.setExecuting(0)
    }

    override fun executionEnded(rope: RoPE) {
        gameView.hideHighlight()
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

    private fun convertToRoPEActions(program: Program): List<RoPE.Action> {
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

    private fun returnToPreviousActivity() {
        this.finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(gameView.width, gameView.height))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val myImageAnalyser = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
//                .setTargetResolution(Size(4000,4000))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    val handler = HandlerCompat.createAsync(Looper.getMainLooper())
                    it.setAnalyzer(
                        ContextCompat.getMainExecutor(this),
                        MyImageAnalyser(
                            imageToBitmapConverter,
                            bitmapToBlocksConverter,
                            handler
                        ) { blocks ->
                            program = ProgramFactory.findSequence(blocks)
                            updateViewWithProgram()
                        })
                }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
//                myImageAnalyser,
                imageCapture
            )

            startTakingPictures()
        }

        cameraProviderFuture.addListener(
            cameraProviderFutureListener,
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun startTakingPictures() {
        val executor = Executors.newSingleThreadExecutor()
        val handler = HandlerCompat.createAsync(Looper.getMainLooper())

        val callback = object : ImageCapture.OnImageCapturedCallback() {
            @SuppressLint("UnsafeExperimentalUsageError")
            override fun onCaptureSuccess(image: ImageProxy) {
                try {

                    val bitmap = convertToBitmap(image)

                    //val bitmap = image.image?.let { imageToBitmapConverter.convertToBitmap(it) }
                    val blocks = bitmapToBlocksConverter.convertBitmapToBlocks(bitmap!!)
                    handler.post {
                        program = ProgramFactory.findSequence(blocks)
                        updateViewWithProgram()
                    }
                } catch (e: Exception) {
                    e.message?.let { Log.e(javaClass.simpleName, it) }
                } finally {
                    image.close()
                    handler.post {
                        imageCapture.takePicture(executor, this)
                    }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
            }
        }
        imageCapture.takePicture(executor, callback)
    }

    private fun convertToBitmap(image: ImageProxy): Bitmap? {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity()).also { buffer.get(it) }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun getLevel(taskIndex: Int) =
        if (levels.size > taskIndex) levels[taskIndex] else Level()

    private fun startLevel(level: Level) {
        matView.mat = level.mat
        gameView.matView = matView
    }

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA
        )
    }
}