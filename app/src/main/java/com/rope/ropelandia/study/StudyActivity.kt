package com.rope.ropelandia.study

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rope.ropelandia.R
import com.rope.ropelandia.game.BlockToBlockView
import com.rope.ropelandia.game.LevelLoader
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.ForwardBlock
import com.rope.ropelandia.model.Program
import kotlinx.android.synthetic.main.activity_study.*

class StudyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        gameView.blocksViews = createCurvedProgram().blocks.map {
            BlockToBlockView.convert(this, it)
        }
        val levels = LevelLoader.load(this)
        gameView.setMat(levels[0].mat)
    }

    private fun createTestProgram(): Program {

        val startY = 600f

        return Program(listOf(
            ForwardBlock(600f, startY, 10f, 0f),
            ForwardBlock(600f, startY - Block.HEIGHT, 10f, 0f),
            ForwardBlock(600f, startY - Block.HEIGHT * 2, 10f, 0f),
            ForwardBlock(600f, startY - Block.HEIGHT * 3, 10f, 0f),
            ForwardBlock(600f, startY - Block.HEIGHT * 4, 10f, 0f)
        ))
    }

    private fun createCurvedProgram(): Program {

        val blocks = mutableListOf<Block>()

        val yDislocation = Block.HEIGHT
        val xDislocation = Block.WIDTH * 0.2f
        val angleDislocation = 0.1f

        for (i in 0..5) {
            blocks.add(ForwardBlock(
                600f - xDislocation * i,
                600f - yDislocation * i,
                10f,
                0f + angleDislocation * i
            ))
        }

        return Program(blocks)

    }

}