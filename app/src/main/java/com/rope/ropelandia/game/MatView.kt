package com.rope.ropelandia.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class MatView(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet) {

    var mat = arrayOf(
        arrayOf(0, 0, 0, 0),
        arrayOf(4, 5, 6, 0),
        arrayOf(3, 2, 7, 8),
        arrayOf(0, 1, 0, 0)
    )
        set(value) {
            field = value
            updateSquares(height)
            invalidate()
        }

    data class Square(val squareValue: Int, val matLine: Int, val matColumn: Int, val rect: Rect) {
        enum class Type {
            EMPTY, PATH
        }

        val type: Type = when (squareValue) {
            in 1..99 -> Type.PATH
            else -> Type.EMPTY
        }
    }

    private val squares = mutableListOf<Square>()

    init {
        setBackgroundColor(Color.GRAY)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateSquares(h)
        if (mat.isNotEmpty()) {
            updatePathWidth(h)
        }
    }

    private fun updatePathWidth(h: Int) {
        // the path where to toy will run is a fewer than a square width
        val relativeWidth = 0.7f
        pathPaint.strokeWidth = h / mat.size * relativeWidth
    }

    private fun updateSquares(viewHeight: Int) {
        val matLines = mat.size
        val squareSize = viewHeight / matLines
        squares.clear()
        mat.forEachIndexed { matLine, line ->
            line.mapIndexed { matColumn, squareValue ->
                val rect = createSquareRect(squareSize, matLine, matColumn)
                Square(squareValue, matLine, matColumn, rect)
            }.also { lineSquareList ->
                squares.addAll(lineSquareList)
            }
        }
    }

    private fun createSquareRect(squareSize: Int, matLine: Int, matColumn: Int): Rect {
        val left = matColumn * squareSize
        val top = matLine * squareSize
        val right = left + squareSize
        val bottom = top + squareSize
        return Rect(left, top, right, bottom)
    }

    private val rectPaint = Paint().apply {
        color = Color.argb(250, 222, 170, 135)
    }

    private val rectBorderPaint = Paint().apply {
        color = Color.WHITE
    }

    private val pathPaint = Paint().apply {
        strokeWidth = 20.0f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(100f)
        color = Color.argb(207, 200, 113, 55)
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        isDither = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (mat.isNotEmpty() && canvas != null) {
            drawMatrix(canvas)
            drawPath(canvas)
        }
    }

    private fun drawPath(canvas: Canvas) {
        val path = createPath()
        canvas.drawPath(path, pathPaint)
    }

    private fun createPath(): Path {
        val path = Path()

        squares
            .filter { it.type == Square.Type.PATH }
            .sortedBy { it.squareValue }
            .takeIf { it.isNotEmpty() }
            ?.let { pathSquares ->
                goToStart(pathSquares[0], path)
                pathSquares.forEach { lineTo(it, path) }
            }

        return path
    }

    private fun goToStart(startSquare: Square, path: Path) {
        val startX = startSquare.rect.centerX().toFloat()
        val startY = startSquare.rect.centerY().toFloat()
        path.moveTo(startX, startY)
    }

    private fun lineTo(it: Square, path: Path) {
        val x = it.rect.centerX().toFloat()
        val y = it.rect.centerY().toFloat()
        path.lineTo(x, y)
    }

    private fun drawMatrix(canvas: Canvas) {
        val borderWidth = 6
        squares.forEach {
            borderSquare(canvas, it)
            fillSquare(it, borderWidth, canvas)
        }
    }

    private fun borderSquare(canvas: Canvas, square: Square) {
        canvas.drawRect(square.rect, rectBorderPaint)
    }

    private fun fillSquare(it: Square, borderWidth: Int, canvas: Canvas) {
        val left = it.rect.left + borderWidth
        val top = it.rect.top + borderWidth
        val right = it.rect.right - borderWidth
        val bottom = it.rect.bottom - borderWidth

        val fillRect = Rect(left, top, right, bottom)

        canvas.drawRect(fillRect, rectPaint)
    }

}