package com.rope.ropelandia.game.tiles

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.view.isVisible
import com.rope.ropelandia.game.Square

abstract class Tile(val square: Square, height: Int, width: Int, context: Context) : View(context) {
    abstract fun reactToCollision()
    abstract fun getDrawable(): Drawable

    val squareSize = height

    private val rect by lazy {
        val left = square.column * height
        val top = square.line * height
        val right = left + height
        val bottom = top + height
        Rect(left, top, right, bottom)
    }

    private val border = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLUE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(isVisible) {
            canvas?.let {
                getDrawable().apply {
                    bounds = rect
                    draw(canvas)
                    canvas.drawRect(bounds, border)
                }
            }
        }
    }

}