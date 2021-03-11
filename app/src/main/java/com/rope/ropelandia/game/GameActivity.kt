package com.rope.ropelandia.game

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.Size
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
import com.rope.ropelandia.game.bitmaptaker.BitmapTaker
import com.rope.ropelandia.game.bitmaptaker.BitmapTakerFactory
import com.rope.ropelandia.game.blocksanalyser.*
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.RoPEBlock
import kotlinx.android.synthetic.main.main_activity.*
import java.util.concurrent.Executors

class GameActivity : AppCompatActivity(),
    RoPEDisconnectedListener,
    RoPEStartPressedListener,
    RoPEActionListener,
    RoPEExecutionStartedListener,
    RoPEExecutionFinishedListener {

    private val jumpSound by lazy { MediaPlayer.create(applicationContext, R.raw.jump_sound) }
    private val errorSound by lazy { MediaPlayer.create(applicationContext, R.raw.error_sound) }
    private lateinit var bitmapTaker: BitmapTaker
    private val permissionChecker by lazy { PermissionChecker() }
    private val game by lazy { GameLoader.load(applicationContext) }
    private var program: RoPE.Program = SequentialProgram(listOf())
    private val rope by lazy { app.rope!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setupRopeListeners()
        startCameraOrRequestPermission()
        startGame()
    }

    override fun onStop() {
        super.onStop()
        bitmapTaker.stop()
        rope.removeDisconnectedListener(this)
        rope.removeStartPressedListener(this)
        rope.removeActionListener(this)
        rope.removeExecutionStartedListener(this)
        rope.removeExecutionFinishedListener(this)
    }

    private fun updateViewWithBlocks(blocks: List<Block>) {
        val programBlocksViews = blocks.map { block ->
            BlockToBlockView.convert(this, block)
        }
        gameView.programBlocks = programBlocksViews
        gameView.invalidate()
    }

    private fun setupRopeListeners() {
        rope.onDisconnected(this)
        rope.onStartedPressed(this)
        rope.onActionExecuted(this)
        rope.onExecutionStarted(this)
        rope.onExecutionFinished(this)
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

    override fun actionExecuted(rope: RoPE, action: RoPE.Action) {
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
        rope.execute(program)
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

            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels
            val screenSize = Size(width, height)

            val bitmapToBlocksConverter = BitmapToBlocksConverter(screenSize)
            val bitmapTakerExecutor = Executors.newFixedThreadPool(2)

            val blocksAnalyser = BlocksAnalyzerComposite()
            blocksAnalyser.addBlocksAnalyzer(createProgramDetector())
            blocksAnalyser.addBlocksAnalyzer(createMovementsDetector(screenSize))
            blocksAnalyser.addBlocksAnalyzer(createFaceDetector())
            blocksAnalyser.addBlocksAnalyzer(object : BlocksAnalyzer {
                override fun analyze(blocks: List<Block>) {
                    blocks.filterIsInstance<RoPEBlock>().forEach {
                        runOnUiThread {
                            gameView.ropeView.x = it.centerX
                            gameView.ropeView.y = it.centerY
                            gameView.invalidate()
                        }
                    }
                }
            })

            val bitmapTookCallback = object : BitmapTaker.BitmapTookCallback {
                override fun onBitmap(bitmap: Bitmap) {
                    bitmapToBlocksExecutor.submit {
                        try {
                            val blocks = bitmapToBlocksConverter.convertBitmapToBlocks(bitmap)
                            blocksAnalyser.analyze(blocks)
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

    private fun createProgramDetector() = object : ProgramDetector(app.rope!!) {
        private val blocksFoundHandler = HandlerCompat.createAsync(Looper.getMainLooper())

        override fun onFoundProgramBlocks(blocks: List<Block>, program: RoPE.Program) {
            blocksFoundHandler.post {
                this@GameActivity.program = program
                updateViewWithBlocks(blocks)
            }
        }
    }

    private fun createMovementsDetector(screenSize: Size) =
        object : RoPESquareDetector(game, screenSize) {
            override fun changedSquare(squareX: Int, squareY: Int) {
                game.ropePosition.squareX = squareX
                game.ropePosition.squareY = squareY
                Thread {
                    jumpSound.start()
                }.start()

                rope.let {
                    val goingForward = it.nextActionIs(RoPE.Action.FORWARD)
                    val ropeX = game.ropePosition.squareX
                    val ropeY = game.ropePosition.squareY
                    val nextPosition = when (game.ropePosition.face) {
                        Position.Face.NORTH -> game.currentMat().subMat(ropeX, ropeY - 1)
                        Position.Face.SOUTH -> game.currentMat().subMat(ropeX, ropeY + 1)
                        Position.Face.WEST -> game.currentMat().subMat(ropeX - 1, ropeY)
                        Position.Face.EAST -> game.currentMat().subMat(ropeX + 1, ropeY)
                        else -> throw IllegalStateException("The toy face is undefined")
                    }
                    if (goingForward && nextPosition.hasTile(Tile.TileType.OBSTACLE)) {
                        Thread { errorSound.start() }.start()
                    }
                }
            }
        }

    private fun createFaceDetector() = object : RoPEFaceDetector() {
        override fun changedFace(face: Position.Face) {
            game.ropePosition.face = face
        }
    }

    private fun startGame() {
        gameView.matView.mat = game.currentMat()
    }

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA
        )
    }
}