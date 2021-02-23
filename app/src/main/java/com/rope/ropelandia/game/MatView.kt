package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View

typealias Tile = Drawable
typealias MatLayer = Array<Array<Tile>>
typealias Mat = MutableList<MatLayer>

class MatView(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet) {

    var mat: Mat = mutableListOf()

    private var squareSize = 100

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLUE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateSquareSize(h)
    }

    private fun updateSquareSize(height: Int) {
        if (mat.isNotEmpty()) {
            val matLayer: MatLayer = mat[0]
            val lines = matLayer.size
            squareSize = height / lines
        }
    }

    private fun createRect(squareSize: Int, matLine: Int, matColumn: Int): Rect {
        val left = matColumn * squareSize
        val top = matLine * squareSize
        val right = left + squareSize
        val bottom = top + squareSize
        return Rect(left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.rotate(180f, (width / 2).toFloat(), (height / 2).toFloat())
            mat.forEach { layer ->
                layer.forEachIndexed { lineIndex, line ->
                    line.forEachIndexed { columnIndex, tile ->
                        drawTile(canvas, lineIndex, columnIndex, tile)
                    }
                }
            }
            it.rotate(-180f, (width / 2).toFloat(), (height / 2).toFloat())
        }
    }

    private fun drawTile(canvas: Canvas, lineIndex: Int, columnIndex: Int, tile: Drawable) {
        val rect = createRect(squareSize, lineIndex, columnIndex)
        tile.apply {
            bounds = rect
            draw(canvas)
        }
        canvas.drawRect(rect, paint)
    }

}