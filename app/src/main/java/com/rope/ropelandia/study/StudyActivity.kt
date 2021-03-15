package com.rope.ropelandia.study

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Looper
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat
import com.rope.connection.RoPE
import com.rope.connection.ble.RoPEActionListener
import com.rope.connection.fake.RoPEFinderFake
import com.rope.ropelandia.R
import com.rope.ropelandia.capture.BlocksToProgramConverter
import com.rope.ropelandia.capture.BlockSequenceFinder
import com.rope.ropelandia.game.GameView
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.ForwardBlock
import kotlinx.android.synthetic.main.activity_study.*

class RoPESensors {
    fun obstacleAhead(): Boolean {
        return Math.random() > 0.8f
    }
}

class StudyActivity : AppCompatActivity() {

    private var rope: RoPE? = null
    private val handler by lazy { HandlerCompat.createAsync(Looper.getMainLooper()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val blocks = retrieveBlocks()
//        gameView.programBlocks = blocks.map {
//            BlockToBlockView.convert(this, it)
//        }
//        val game = GameLoader.load(this)
//        gameView.setMat(game.currentMat())

        val ropeFinder = RoPEFinderFake(this, handler)
        val ropeSensors = RoPESensors()

        ropeFinder.onRoPEFound {
            rope = it
            setupBehaviour(it, ropeSensors)
            setupScreenBehaviour(it, ropeSensors, gameView)
        }
        ropeFinder.findRoPE()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if(it.action == MotionEvent.ACTION_DOWN){
                val blocks = retrieveBlocks()
                val blocksSequence = BlockSequenceFinder.findSequence(blocks)
                val program = BlocksToProgramConverter.convert(blocksSequence)
                rope?.execute(program)
            }
        }
        return true
    }

    private fun setupScreenBehaviour(rope: RoPE, ropeSensors: RoPESensors, gameView: GameView) {
//        rope.onActionExecuted(object : RoPEActionListener {
//            override fun actionExecuted(rope: RoPE, action: RoPE.Action) {
//                gameView.hideHighlight()
//                gameView.highlight(rope.actionIndex + 1)
//            }
//        })
//        rope.onExecutionStarted(object : RoPEExecutionStartedListener {
//            override fun executionStarted(rope: RoPE) {
//                gameView.highlight(0)
//            }
//        })
    }

    private fun setupBehaviour(
        rope: RoPE,
        ropeSensors: RoPESensors
    ) {
        rope.onActionExecuted(object : RoPEActionListener {
            override fun actionExecuted(rope: RoPE, action: RoPE.Action) {
                if (rope.nextActionIs(RoPE.Action.FORWARD) && ropeSensors.obstacleAhead()) {
                    rope.stop()
                }
            }
        })
    }

    private fun retrieveBlocks(): List<Block> {

        val blocks = mutableListOf<Block>()

        val yDislocation = Block.HEIGHT
        val xDislocation = Block.WIDTH * 0.2f
        val angleDislocation = 0.1f
        var variationY = 1.0f
        var variationX = 1.0f
        for (i in 1..5) {
            blocks.add(
                ForwardBlock(
                    600f - xDislocation * i * variationX,
                    600f - yDislocation * i * variationY,
                    10f,
                    0f + angleDislocation * i
                )
            )
            variationY *= 0.8f
            variationX *= 1.2f
        }

        return blocks
    }



}