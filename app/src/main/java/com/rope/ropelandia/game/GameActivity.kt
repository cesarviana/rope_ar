package com.rope.ropelandia.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
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
import com.rope.ropelandia.R
import com.rope.ropelandia.app
import com.rope.ropelandia.ctpuzzle.*
import com.rope.ropelandia.game.bitmaptaker.BitmapTaker
import com.rope.ropelandia.game.bitmaptaker.BitmapTakerFactory
import com.rope.ropelandia.game.blocksanalyser.*
import com.rope.ropelandia.game.converters.BitmapToBlocksConverter
import com.rope.ropelandia.game.converters.ParticipationToGameConverter
import com.rope.ropelandia.game.views.GameView
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.RoPEBlock
import com.rope.ropelandia.usecases.perspectiverectangle.data.repositories.PerspectiveRectangleRepositoryImpl
import com.rope.ropelandia.usecases.perspectiverectangle.domain.usecases.GetPerspectiveRectangle
import com.rope.ropelandia.usecases.perspectiverectangle.domain.usecases.StorePerspectiveRectangle
import kotlinx.android.synthetic.main.main_activity.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

private const val TAG = "GAME_ACTIVITY"

class GameActivity : AppCompatActivity(),
    RoPEDisconnectedListener,
    RoPEStartPressedListener,
    RoPEActionListener,
    RoPEExecutionStartedListener,
    RoPEExecutionFinishedListener {

    private val rope by lazy { app.rope!! }
    private var requiredStart = false

    private val myExecutor by lazy {
        val executor = ThreadPoolExecutor(
            1, 1, 0L,
            TimeUnit.MILLISECONDS, LinkedBlockingQueue()
        )
        executor.rejectedExecutionHandler = RejectedExecutionHandler { _, _ ->
            Log.d(TAG, "Fail on executor")
        }
        executor
    }
    private val permissionChecker by lazy { PermissionChecker() }
    private var bitmapTaker: BitmapTaker? = null
    private lateinit var game: Game
    private lateinit var participation: Participation
    private val attempts = mutableListOf<Attempt>()
    private var initialTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        rope.apply {
            stop()
            sendActions(
                actionList = listOf(
                    RoPE.Action.SOUND_ON,
                    RoPE.Action.INACTIVE_DIRECTIONAL_BUTTONS,
                    RoPE.Action.ACTIVE_CONNECTION
                )
            )
        }
        setupRopeListeners()
        CtPuzzleApi.initialize(this)
        Sounds.initialize(this)
        loadGame { game, participation ->
            registerStartedLevel(game, participation)
            this.game = game
            this.participation = participation
            initialTime = currentTimeInSeconds()
            runOnUiThread {
                updateView(game, gameView)
                setupGameListeners(game)
                startCameraOrRequestPermission()
            }
        }
        Sounds.play(Sounds.backgroundHappy, looping = true)
    }

    override fun onResume() {
        super.onResume()
        Sounds.initialize(this)
        Sounds.play(Sounds.backgroundHappy, looping = true)
    }

    override fun onDestroy() {
        super.onDestroy()
        Sounds.stop(Sounds.backgroundHappy)
    }

    override fun onStop() {
        super.onStop()
        rope.sendActions(
            listOf(
                RoPE.Action.ACTIVE_DIRECTIONAL_BUTTONS,
                RoPE.Action.INACTIVE_CONNECTION
            )
        )
        bitmapTaker?.stop()
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
        val handler = HandlerCompat.createAsync(Looper.getMainLooper())
        game.onLevelFinished {
            registerResponse(game, participation)
            Sounds.decreaseBackgroundVolume()
            Sounds.play(Sounds.levelEnd) {
                if (game.hasAnotherLevel()) {
                    game.goToNextLevel()
                    registerStartedLevel(game, participation)
                    Sounds.resetBackgroundVolume()
                } else {
                    Sounds.play(Sounds.gameEnd) {
                        Sounds.resetBackgroundVolume()
                    }
                }
            }
        }
        game.onArrivedAtSquare { square: Square ->
            handler.post {
                Sounds.decreaseBackgroundVolume()
                game.getTilesAt(square).forEach {
                    it.reactToCollision()
                }
                Sounds.resetBackgroundVolume()
                game.checkLevelFinished()
            }
        }
    }

    private fun loadGame(callback: (game: Game, participation: Participation) -> Unit) {
        val dataUrl = intent.extras?.getString("dataUrl") ?: DEFAULT_CTPUZZLE_DATA_URL

        try {
            CtPuzzleApi.newParticipation(dataUrl) {
                val game = ParticipationToGameConverter.convert(this, it)
                callback.invoke(game, it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error when loading game: ${e.message}")
        }
    }

    override fun disconnected(rope: RoPE) {
        Sounds.decreaseBackgroundVolume()
        Sounds.play(Sounds.connectionFailed) {
            returnToPreviousActivity()
        }
    }

    override fun startPressed(rope: RoPE) {
        requiredStart = true
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

    private fun updateViewForRoPEEvent(rope: RoPE) =
        rope.handler.postAtFrontOfQueue {
            updateView(game, gameView)
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
        val commands = program.actionList.map { it.name }
        val seconds = currentTimeInSeconds() - initialTime
        attempts.add(Attempt(commands, timeInSeconds = seconds))
    }

    private fun currentTimeInSeconds() = System.currentTimeMillis() / 1000

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
            if (!isDestroyed) {
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    bitmapTaker?.getUseCase()
                )
                bitmapTaker?.startTakingImages()
            }
        }

        cameraProviderFuture.addListener(
            cameraProviderFutureListener,
            ContextCompat.getMainExecutor(this)
        )
    }

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
            .addBlocksAnalyzer(createSnailDetector())

        private val bitmapToBlocksConverter by lazy {
            val sharedPreferences =
                applicationContext.getSharedPreferences("ROPE_AR", Context.MODE_PRIVATE)
            val repository = PerspectiveRectangleRepositoryImpl(sharedPreferences)
            val getPerspectiveRectangle = GetPerspectiveRectangle(repository)
            val storePerspectiveRectangle = StorePerspectiveRectangle(repository)
            BitmapToBlocksConverter(screenSize, getPerspectiveRectangle, storePerspectiveRectangle)
        }

        override fun onBitmap(bitmap: Bitmap) {
            try {
                val blocks = bitmapToBlocksConverter.convertBitmapToBlocks(bitmap)
                blocksAnalyser.analyze(blocks)
            } catch (e: java.lang.Exception) {
                e.message?.let { Log.e(TAG, it) }
            }
        }

        override fun onError(e: Exception) {
            Log.e(TAG, "Error when getting bitmap")
        }
    }

    private fun createBitmapTaker(
        bitmapTookCallback: BitmapTaker.BitmapTookCallback,
        cameraProvider: ProcessCameraProvider
    ): BitmapTaker {
        val bitmapTakerType = decideBitmapTakerType()

        val bitmapTaker = BitmapTakerFactory()
            .createBitmapTaker(
                this,
                myExecutor,
                bitmapTakerType,
                bitmapTookCallback
            )

        bitmapTaker.onStopping {
            myExecutor.shutdownNow()
            cameraProvider.unbindAll()
        }

        return bitmapTaker
    }

    private fun decideBitmapTakerType(): BitmapTakerFactory.Type {
        return BitmapTakerFactory.Type.VIDEO
    }

    private fun createProgramDetector() = object : ProgramDetector(app.rope!!) {
        private val blocksFoundHandler = HandlerCompat.createAsync(Looper.getMainLooper())

        override fun onFoundProgramBlocks(blocks: List<Block>, program: RoPE.Program) {
            blocksFoundHandler.post {
                if (rope.isStopped()) {
                    game.updateProgramBlocks(blocks)
                    updateView(game, gameView)
                    if (requiredStart) {
                        requiredStart = false
                        ropeExecute(program)
                    }
                }
            }
        }
    }

    private fun createMovementsDetector(screenSize: Size) =
        object : RoPESquareDetector(game, screenSize) {
            override fun changedSquare(squareX: Int, squareY: Int) {
                game.updateRoPEPosition(squareX, squareY)
            }
        }

    private fun createDirectionDetector() = object : RoPEDirectionDetector() {
        override fun changedFace(direction: Position.Direction) {
            game.ropePosition.direction = direction
        }
    }

    private fun createCoordinateDetector() = object : BlocksAnalyzer {
        override fun analyze(blocks: List<Block>) {
            blocks.filterIsInstance<RoPEBlock>().forEach {
                game.updateCoordinate(it.centerX, it.centerY)
            }
        }
    }

    private fun createSnailDetector() = object : SnailDetector() {
        override fun snailArrivedNearBlocks() {
            if (game.programIsExecuting) {
                rope.stop()
                // highglight next action
                // light next button
                // enable button x
                // <pressed:x>
                // <cmds:xe>
            }
        }
    }

    private fun updateView(game: Game, gameView: GameView) {
        gameView.update(game)
    }

    private fun registerStartedLevel(game: Game, participation: Participation) {
        val item = participation.getTestItem(game.levelIndex)
        tryApiCall {
            CtPuzzleApi.registerProgress(participation, item)
        }
    }

    private fun registerResponse(game: Game, participation: Participation) {
        val response = ResponseForItem(attempts)
        val item = participation.getTestItem(game.levelIndex)
        tryApiCall {
            CtPuzzleApi.registerResponse(participation, item, response)
        }
        attempts.clear()
    }

    private fun tryApiCall(apiCall: () -> Unit) {
        try {
            apiCall.invoke()
        } catch (e: Exception) {
            Log.e(TAG, "Error on API call: ${e.message}")
        }
    }

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA
        )
    }
}