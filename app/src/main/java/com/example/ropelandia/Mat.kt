package com.example.ropelandia

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView

class Mat(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs) {

    var blocks = listOf<Block>()

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSPARENT)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            blocks.forEach {
                it.paint(canvas)
            }
        }
    }

}