package com.rope.ropelandia.game

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat
import com.rope.connection.RoPE
import com.rope.connection.ble.*
import com.rope.droideasy.PermissionChecker
import com.rope.program.SequentialProgram
import com.rope.ropelandia.R
import com.rope.ropelandia.app
import com.rope.ropelandia.capture.BitmapToBlocksConverter
import com.rope.ropelandia.capture.BlocksToProgramConverter
import com.rope.ropelandia.capture.ProgramFactory
import com.rope.ropelandia.game.bitmaptaker.BitmapTaker
import com.rope.ropelandia.game.bitmaptaker.BitmapTakerFactory
import com.rope.ropelandia.model.Block
import kotlinx.android.synthetic.main.main_activity.*
import java.util.concurrent.Executors

class GameActivity : AppCompatActivity(),
    RoPEDisconnectedListener,
    RoPEStartPressedListener,
    RoPEActionListener,
    RoPEExecutionStartedListener,
    RoPEExecutionFinishedListener {

    private lateinit var bitmapTaker: BitmapTaker
    private val permissionChecker by lazy { PermissionChecker() }
    private val levels: List<Level> by lazy { LevelLoader.load(applicationContext) }
    private var levelIndex = 0
    private var program: RoPE.Program = SequentialProgram(listOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setupRopeListeners()
        startCameraOrRequestPermission()
        startLevel(getLevel(levelIndex))
    }

    override fun onStop() {
        super.onStop()
        bitmapTaker.stop()
        app.rope?.removeDisconnectedListener(this)
        app.rope?.removeStartPressedListener(this)
        app.rope?.removeActionListener(this)
        app.rope?.removeExecutionStartedListener(this)
        app.rope?.removeExecutionFinishedListener(this)
    }

    private fun updateViewWithBlocks(blocks: List<Block>) {
        val programBlocksViews = blocks.map { block ->
            BlockToBlockView.convert(this, block)
        }
        gameView.blocksViews = programBlocksViews
        gameView.invalidate()
    }

    private fun setupRopeListeners() {
        app.rope?.onDisconnected(this)
        app.rope?.onStartedPressed(this)
        app.rope?.onActionExecuted(this)
        app.rope?.onExecutionStarted(this)
        app.rope?.onExecutionFinished(this)
    }

    override fun disconnected(rope: RoPE) {
        returnToPreviousActivity()
    }

    override fun startPressed(rope: RoPE) {
        ropeExecute(program)
    }

    override fun executionStarted(rope: RoPE) {
        rope.handler.postAtFrontOfQueue {
            gameView.setExecuting(0)
        }
    }

    override fun actionExecuted(rope: RoPE) {
        val nextAction = rope.actionIndex + 1
        rope.handler.postAtFrontOfQueue {
            gameView.hideHighlight()
            gameView.setExecuting(nextAction)
        }
    }

    override fun executionEnded(rope: RoPE) {
        rope.handler.post {
            gameView.hideHighlight()
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

    private fun ropeExecute(program: RoPE.Program) {
        app.rope?.execute(program)
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

            val bitmapTookHandler = HandlerCompat.createAsync(Looper.getMainLooper())
            val bitmapToBlocksExecutor = Executors.newFixedThreadPool(4)
            val blocksFoundHandler = HandlerCompat.createAsync(Looper.getMainLooper())

            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels

            val bitmapToBlocksConverter = BitmapToBlocksConverter(height, width)
            val bitmapTakerExecutor = Executors.newFixedThreadPool(2)

            val bitmapTookCallback = object: BitmapTaker.BitmapTookCallback {
                override fun onBitmap(bitmap: Bitmap) {
                    val ropeExecuting = app.rope?.isStopped() == false
                    if(ropeExecuting)
                        return
                    bitmapToBlocksExecutor.submit {
                        try {
                            val blocks = bitmapToBlocksConverter.convertBitmapToBlocks(bitmap)
                            if (blocks.isNotEmpty()) { // ignore if no block found
                                blocksFoundHandler.post {
                                    val blockSequence = ProgramFactory.findSequence(blocks)
                                    updateViewWithBlocks(blockSequence)
                                    program = BlocksToProgramConverter.convert(blockSequence)
                                }
                            }
                        } catch (e: java.lang.Exception) {
                            e.message?.let { Log.e("GAME_ACTIVITY", it) }
                        }

                    }.get()
                }

                override fun onError(e: Exception) {
                    Log.e(javaClass.simpleName, "Error when getting bitmap")
                    e.printStackTrace()
                }
            }

            bitmapTaker = BitmapTakerFactory()
                .createBitmapTaker(
                    this,
                    bitmapTookHandler,
                    bitmapTakerExecutor,
                    BitmapTakerFactory.Type.PICTURE,
                    bitmapTookCallback
                )

            val useCase = bitmapTaker.getUseCase()

            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                useCase
            )

            bitmapTaker.startTakingImages()
        }

        cameraProviderFuture.addListener(
            cameraProviderFutureListener,
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun getLevel(taskIndex: Int) =
        if (levels.size > taskIndex) levels[taskIndex] else Level()

    private fun startLevel(level: Level) {
        gameView.matView.mat = level.mat
    }

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA
        )
    }
}