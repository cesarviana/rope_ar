package com.rope.ropelandia.study

import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.rope.ropelandia.R
import com.rope.ropelandia.model.ForwardBlock
import com.rope.ropelandia.model.Program
import topcodes.TopCodesScanner

class StudyActivity : AppCompatActivity() {
    private var actionIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
      //  Toast.makeText(this, stringFromJNI(), Toast.LENGTH_LONG).show()
    }

    external fun stringFromJNI(): String

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    private fun createTestProgram(): Program {
        val block = ForwardBlock(300f, 300f, 10f, 2f)
        val block2 = ForwardBlock(600f, 600f, 10f, 4f)

        return Program(listOf(block, block2))
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val bitmap = BitmapFactory.decodeResource(resources, R.raw.topcodes)
        val topcodes = TopCodesScanner().searchTopCodes(bitmap)
        return super.onTouchEvent(event)
    }

}