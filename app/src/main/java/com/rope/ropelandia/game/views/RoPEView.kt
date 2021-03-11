package com.rope.ropelandia.game.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class RoPEView(context: Context) : View(context) {

    private val paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            drawCircle(x, y, 20f, paint)
        }
    }

}