package com.rope.ropelandia.study

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.rope.ropelandia.R
import com.rope.ropelandia.model.ForwardBlock
import com.rope.ropelandia.model.Program
import kotlinx.android.synthetic.main.activity_study.*

class StudyActivity : AppCompatActivity() {
    private var actionIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        val block = ForwardBlock(300f, 300f, 10f, 2f)
        val block2 = ForwardBlock(600f, 600f, 10f, 4f)

        mat.program = Program(listOf(block, block2))

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (event.action == MotionEvent.ACTION_UP) {
                if (actionIndex >= mat.program.blocks.size) {
                    actionIndex = 0
                    mat.hideHighlight()
                } else {
                    mat.highlight(actionIndex)
                    actionIndex++
                }
            }
        }
        return super.onTouchEvent(event)
    }

}