package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.rope.ropelandia.R

private const val DEFAULT_SQUARE_SIZE = 300

private typealias MatLayer = List<List<Drawable>>
private typealias Mat = MutableList<MatLayer>

private fun Mat.numberOfLines(): Int {
    val noLayer = this.size == 0
    return if (noLayer) {
        0
    } else {
        val layer = this[0]
        layer.size
    }
}

class MatView(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet) {

    private val mat: Mat = mutableListOf()

    init {
        setBackgroundColor(Color.YELLOW)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    var squareSize = DEFAULT_SQUARE_SIZE

    private fun calcSquareSize(): Int {
        val numberOfLines = mat.numberOfLines()
        if (numberOfLines == 0)
            return DEFAULT_SQUARE_SIZE
        val heightPixels = resources.displayMetrics.heightPixels
        return heightPixels / numberOfLines
    }

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLUE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mat.isNotEmpty()) {
            canvas?.let {
                drawFloor(canvas)
                drawElements(canvas)
            }
        }
    }

    private fun drawFloor(canvas: Canvas) {
        mat[0].forEachIndexed { lineIndex, line ->
            line.forEachIndexed { columnIndex, _ ->
                drawTile(canvas, lineIndex, columnIndex, floor)
            }
        }
    }

    private fun drawElements(canvas: Canvas) {
        mat.forEach { lines ->
            lines.forEachIndexed { lineIndex, line ->
                line.forEachIndexed { columnIndex, tile ->
                    drawTile(canvas, lineIndex, columnIndex, tile)
                }
            }
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

    private fun createRect(squareSize: Int, matLine: Int, matColumn: Int): Rect {
        val left = matColumn * squareSize
        val top = matLine * squareSize
        val right = left + squareSize
        val bottom = top + squareSize
        return Rect(left, top, right, bottom)
    }

    private val empty = ResourcesCompat.getDrawable(context.resources, R.drawable.empty, null)!!
    private val path = ResourcesCompat.getDrawable(context.resources, R.drawable.path, null)!!
    private val apple = ResourcesCompat.getDrawable(context.resources, R.drawable.apple, null)!!
    private val floor = ResourcesCompat.getDrawable(context.resources, R.drawable.floor, null)!!

    private val tiles = mapOf(
        "null" to empty,
        "path" to path,
        "apple" to apple,
        "floor" to floor
    )

    fun updateMat(stringMat: Array<Array<Array<String>>>) {
        this.mat.clear()

        val layers = stringMat.map { layer ->
            layer.map { line ->
                line.map {
                    tiles[it] ?: tiles["null"]!!
                }
            }
        }

        this.mat.addAll(layers)
        this.squareSize = calcSquareSize()
    }

}