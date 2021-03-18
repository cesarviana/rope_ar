package com.rope.ropelandia.game

import android.annotation.SuppressLint
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
import java.net.URI
import java.util.*
import java.util.concurrent.Executors


class GameActivity : AppCompatActivity(),
    RoPEDisconnectedListener,
    RoPEStartPressedListener,
    RoPEActionListener,
    RoPEExecutionStartedListener,
    RoPEExecutionFinishedListener {

    private val gameEnd by lazy { MediaPlayer.create(applicationContext, R.raw.game_end_sound) }
    private val levelEnd by lazy { MediaPlayer.create(applicationContext, R.raw.level_end_sound) }
    private val backgroundHappy by lazy { MediaPlayer.create(applicationContext, R.raw.background_happy_sound_1) }
    private lateinit var bitmapTaker: BitmapTaker
    private val permissionChecker by lazy { PermissionChecker() }
    private lateinit var game: Game
    private var program: RoPE.Program = SequentialProgram(listOf())
    private val rope by lazy { app.rope!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setupRopeListeners()
        loadGame {
            runOnUiThread {
                game = it
                updateView(game, gameView)
                setupGameListeners(game)
                startCameraOrRequestPermission()
            }
        }
        backgroundHappy.isLooping = true
        playSound(backgroundHappy)
    }

    override fun onStop() {
        super.onStop()
        backgroundHappy.stop()
        bitmapTaker.stop()
        rope.removeDisconnectedListener(this)
        rope.removeStartPressedListener(this)
        rope.removeActionListener(this)
        rope.removeExecutionStartedListener(this)
        rope.removeExecutionFinishedListener(this)
    }

    private fun setupRopeListeners() {
        rope.onDisconnected(this)
        rope.onStartedPressed(this)
        rope.onActionExecuted(this)
        rope.onExecutionStarted(this)
        rope.onExecutionFinished(this)
    }

    private fun setupGameListeners(game: Game) {
        game.onLevelFinished {
            playSound(levelEnd) {
                game.goToNextLevel()
            }
        }
//        game.onGameFinished {
//            playSound(gameEnd)
//        }
//        game.onNewLevelStarted {
//            updateView(game, gameView)
//        }
        game.onGoingTo { square: Square ->
            game.getAssetsAt(square).forEach {
                it.reactToCollision()
            }
            updateView(game, gameView)
        }
    }

    private fun loadGame(callback: GameLoaded) {
        val dataUrl = intent.extras?.getString("dataUrl")

        if (dataUrl == null) {
            GameLoader.load(this, dataUrl = null, callback)
        } else {
            val uuid = UUID.randomUUID()
            val uri = URI.create(dataUrl).resolve(uuid.toString())
            GameLoader.load(this, uri, callback)
        }
    }

    override fun disconnected(rope: RoPE) {
        returnToPreviousActivity()
    }

    override fun startPressed(rope: RoPE) {
        ropeExecute(program)
    }

    override fun executionStarted(rope: RoPE) {
        game.startExecution()
        updateViewForRoPEEvent(rope)
    }

    override fun actionExecuted(rope: RoPE, action: RoPE.Action) {
        /*
         * When rope robot notifies executed action like goForward, the game
         * state is updated to store this movement.
         */
        game.executeAction()
        updateViewForRoPEEvent(rope)

    }

    override fun executionEnded(rope: RoPE) {
        game.endExecution()
        updateViewForRoPEEvent(rope)
    }

    private fun updateViewForRoPEEvent(rope: RoPE) {
        rope.handler.postAtFrontOfQueue {
            updateView(game, gameView)
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

    @SuppressLint("RestrictedApi", "UnsafeExperimentalUsageError")
    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        val cameraProviderFutureListener = Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val bitmapTookCallback = createBitmapTakerCallback()

            bitmapTaker = createBitmapTaker(bitmapTookCallback, cameraProvider)

            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                bitmapTaker.getUseCase()
            )
            bitmapTaker.startTakingImages()
        }

        cameraProviderFuture.addListener(
            cameraProviderFutureListener,
            ContextCompat.getMainExecutor(this)
        )
    }

    private val myExecutor = Executors.newSingleThreadExecutor()

    private fun createBitmapTakerCallback() = object : BitmapTaker.BitmapTookCallback {

        private val screenSize by lazy {
            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels
            Size(width, height)
        }

        private val blocksAnalyser = BlocksAnalyzerComposite()
            .addBlocksAnalyzer(createProgramDetector())
            .addBlocksAnalyzer(createMovementsDetector(screenSize))
            .addBlocksAnalyzer(createDirectionDetector())
            .addBlocksAnalyzer(createCoordinateDetector())


        private val bitmapToBlocksConverter = BitmapToBlocksConverter(screenSize)

        override fun onBitmap(bitmap: Bitmap) {
            try {
                val blocks = bitmapToBlocksConverter.convertBitmapToBlocks(bitmap)
                blocksAnalyser.analyze(blocks)
            } catch (e: java.lang.Exception) {
                e.message?.let { Log.e("GAME_ACTIVITY", it) }
            }
        }

        override fun onError(e: Exception) {
            Log.e(javaClass.simpleName, "Error when getting bitmap")
        }
    }

    private fun createBitmapTaker(
        bitmapTookCallback: BitmapTaker.BitmapTookCallback,
        cameraProvider: ProcessCameraProvider
    ): BitmapTaker {
        val bitmapTakerType = decideBitmapTakerType()
        val bitmapTakerExecutor = Executors.newSingleThreadExecutor()

        val bitmapTaker = BitmapTakerFactory()
            .createBitmapTaker(
                this,
                myExecutor,
                bitmapTakerType,
                bitmapTookCallback
            )

        bitmapTaker.onStopping {
            bitmapTakerExecutor.shutdownNow()
            cameraProvider.unbindAll()
        }

        return bitmapTaker
    }

    private fun decideBitmapTakerType(): BitmapTakerFactory.Type {
        return BitmapTakerFactory.Type.VIDEO
//        val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
//        return if (profile.videoFrameHeight > 720) BitmapTakerFactory.Type.VIDEO else BitmapTakerFactory.Type.PHOTO
    }

    private fun createProgramDetector() = object : ProgramDetector(app.rope!!) {
        private val blocksFoundHandler = HandlerCompat.createAsync(Looper.getMainLooper())

        override fun onFoundProgramBlocks(blocks: List<Block>, program: RoPE.Program) {
            blocksFoundHandler.post {
                this@GameActivity.program = program
                game.updateProgramBlocks(blocks)
                if (rope.isStopped())
                    updateView(game, gameView)
            }
        }
    }

    private fun createMovementsDetector(screenSize: Size) =
        object : RoPESquareDetector(game, screenSize) {
            override fun changedSquare(squareX: Int, squareY: Int) {
                if (rope.isStopped()) { // if running, update squares from rope messages
                    game.updateRoPEPosition(squareX, squareY)
                }
            }
        }

    private fun createDirectionDetector() = object : RoPEDirectionDetector() {
        override fun changedFace(direction: Position.Direction) {
            if (rope.isStopped()) {
                game.ropePosition.direction = direction
            }
        }
    }

    private fun createCoordinateDetector() = object : BlocksAnalyzer {
        override fun analyze(blocks: List<Block>) {
            blocks.filterIsInstance<RoPEBlock>().forEach {
                game.ropePosition.setCoordinate(it.centerX, it.centerY)
            }
        }
    }

    private fun updateView(game: Game, gameView: GameView) {
        gameView.update(game)
    }


    private fun playSound(sound: MediaPlayer, onCompletionListener: MediaPlayer.OnCompletionListener? = null) {
        Thread {
            sound.start()
            onCompletionListener?.let {
                sound.setOnCompletionListener(onCompletionListener)
            }
        }.start()
    }

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA
        )
    }
}