package com.rope.ropelandia.study

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.rope.ropelandia.R
import com.rope.ropelandia.game.BlockView
import com.rope.ropelandia.model.ForwardBlock
import com.rope.ropelandia.model.Program
import kotlinx.android.synthetic.main.activity_study.*
import model.Mat
import model.Task

class StudyActivity : AppCompatActivity() {
    private var actionIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        gameView.blocksViews = createTestProgram().blocks.map { block ->
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
        //gameView.matView = createTestTask()
    }

    private fun createTestProgram(): Program {
        val block = ForwardBlock(300f, 300f, 10f, 2f)
        val block2 = ForwardBlock(600f, 600f, 10f, 4f)

        return Program(listOf(block, block2))
    }

    private fun createTestTask(): Task {
        val mat = Mat(
            arrayOf(
                arrayOf(0, 0, 0, 0),
                arrayOf(0, 0, 0, 0),
                arrayOf(0, 2, 0, 0),
                arrayOf(0, 1, 0, 0)
            )
        )
        return Task("Mapa de um avanço", mat)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (event.action == MotionEvent.ACTION_UP) {
                if (actionIndex >= gameView.blocksViews.size) {
                    actionIndex = 0
                    gameView.hideHighlight()
                } else {
                    gameView.highlight(actionIndex)
                    actionIndex++
                }
            }
        }
        return super.onTouchEvent(event)
    }

}