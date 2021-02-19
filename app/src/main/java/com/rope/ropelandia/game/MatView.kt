package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

private const val SQUARE_SIZE = 30

class MatView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val mat = arrayOf(
        arrayOf(0,0,0,0),
        arrayOf(0,2,0,0),
        arrayOf(0,1,0,0)
    )

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        val squareSize = height / 4

        mat.forEachIndexed { lineIndex, line ->
            line.forEachIndexed { squareIndex, square ->
                val left = squareIndex * squareSize
                //canvas?.drawRect()
            }
        }

    }

}