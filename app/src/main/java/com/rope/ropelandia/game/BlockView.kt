package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

class BlockView(context: Context) : View(context) {

    var highlighted: Boolean = false
    var bounds = Rect()
    var angle = 0.0f

    private val paint = Paint().apply{
        color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if(!highlighted)
            return

        val centerX = bounds.centerX().toFloat()
        val centerY = bounds.centerY().toFloat()
        val radius = bounds.height() / 2f

        canvas?.apply {
            canvas.drawCircle(centerX, centerY, radius, paint)
        }
    }
}