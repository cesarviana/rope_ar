package com.rope.ropelandia.game

import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.rope.connection.RoPE
import com.rope.connection.ble.*
import com.rope.droideasy.PermissionChecker
import com.rope.ropelandia.R
import com.rope.ropelandia.app
import com.rope.ropelandia.model.*
import kotlinx.android.synthetic.main.main_activity.*

class GameActivity : AppCompatActivity(),
    RoPEDisconnectedListener,
    RoPEStartPressedListener,
    RoPEActionListener,
    RoPEExecutionStartedListener,
    RoPEExecutionFinishedListener {

    private var startRequired: Boolean = false
    private val permissionChecker = PermissionChecker()
    private lateinit var levels: List<Level>
    private var levelIndex = 0
    private var program: Program = Program(listOf())
    private val tag = "GAME_VIEW"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setupRopeListeners()
        startCameraOrRequestPermission()
        levels = LevelLoader.load(applicationContext)
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
        if (program.blocks.isEmpty())
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

            val myImageAnalyser = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this), MyImageAnalyser(this) { blocks ->
                        Log.d(javaClass.simpleName, "Blocks found: ${blocks.size}")
                    })
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    myImageAnalyser
                )
            } catch (e: Exception) {
                Log.e(tag, "Use case binding failed", e)
            }
        }

        cameraProviderFuture.addListener(
            cameraProviderFutureListener,
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun getLevel(taskIndex: Int) = if (levels.size > taskIndex) levels[taskIndex] else Level()

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