package com.rope.ropelandia.study

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rope.connection.RoPE
import com.rope.connection.fake.RoPEFinderFake
import com.rope.ropelandia.R
import com.rope.ropelandia.capture.ProgramFactory
import com.rope.ropelandia.game.BlockToBlockView
import com.rope.ropelandia.game.LevelLoader
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.ForwardBlock
import kotlinx.android.synthetic.main.activity_study.*

interface RoPEExecutionManager {
    fun setup(rope: RoPE, ropeProgram: RoPE.Program)
}

class StudyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val blocks = createCurvedProgram()
        gameView.blocksViews = blocks.map {
            BlockToBlockView.convert(this, it)
        }
        val levels = LevelLoader.load(this)
        gameView.setMat(levels[0].mat)

        val ropeFinder = RoPEFinderFake(this)
        ropeFinder.onRoPEFound {
            val program = ProgramFactory.createFromBlocks(blocks)
            it.execute(program)
        }
        ropeFinder.findRoPE()
    }

    private fun createTestProgram(): List<Block> {

        val startY = 600f

        return listOf(
            ForwardBlock(600f, startY, 10f, 0f),
            ForwardBlock(600f, startY - Block.HEIGHT, 10f, 0f),
            ForwardBlock(600f, startY - Block.HEIGHT * 2, 10f, 0f),
            ForwardBlock(600f, startY - Block.HEIGHT * 3, 10f, 0f),
            ForwardBlock(600f, startY - Block.HEIGHT * 4, 10f, 0f)
        )
    }

    private fun createCurvedProgram(): List<Block> {

        val blocks = mutableListOf<Block>()

        val yDislocation = Block.HEIGHT
        val xDislocation = Block.WIDTH * 0.2f
        val angleDislocation = 0.1f
        var variationY = 1.0f
        var variationX = 1.0f
        for (i in 1..5) {
            blocks.add(ForwardBlock(
                600f - xDislocation * i * variationX,
                600f - yDislocation * i * variationY,
                10f,
                0f + angleDislocation * i
            ))
            variationY *= 0.8f
            variationX *= 1.2f
        }

        return blocks
    }

}