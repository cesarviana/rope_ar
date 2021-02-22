package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import java.lang.Exception

typealias Mat = MutableList<MatLayer>
typealias MatLayer = Array<Array<String>>

class MatView(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet) {

    var mat: Mat = mutableListOf()

    private val tiles: MutableMap<String, Drawable> = mutableMapOf()
    private var squareSize = 40

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLUE
    }

    private var background: MatLayer = arrayOf(
        arrayOf("floor", "floor", "floor", "floor"),
        arrayOf("floor", "floor", "floor", "floor"),
        arrayOf("floor", "floor", "floor", "floor"),
        arrayOf("floor", "floor", "floor", "floor")
    )

    private var path: MatLayer = arrayOf(
        arrayOf("",      "",        "", ""),
        arrayOf("", "brpath",       "", ""),
        arrayOf("", "btpath", "btpath", ""),
        arrayOf("", "trpath", "tlpath", "")
    )

    init {
        setBackgroundColor(Color.GRAY)
        addLayer(background)
        addLayer(path)
    }

    fun addLayer(layer: MatLayer) {
        mat.add(layer)
        layer.forEach { line ->
            line.forEach { tileId ->
                if (!tiles.containsKey(tileId)) {
                    tiles[tileId] = readTile(tileId)
                }
            }
        }
    }

    private fun readTile(tileId: String): Drawable {
        if(tileId.isBlank())
            return readTile("floor")

        return try {
            val identifier = resources.getIdentifier(tileId, "drawable", context.packageName)
            ResourcesCompat.getDrawable(resources, identifier, null)!!
        } catch (e: Exception) {
            Log.e("MAT_VIEW", "Read tile $tileId failed.")
            throw e
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        squareSize = h / 4
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
            mat.forEach { layer ->
                layer.forEachIndexed { lineIndex, line ->
                    line.forEachIndexed { columnIndex, tileId ->
                        drawTile(canvas, lineIndex, columnIndex, tileId)
                    }
                }
            }
        }
    }

    private fun drawTile(canvas: Canvas, lineIndex: Int, columnIndex: Int, tileId: String) {
        val rect = createRect(squareSize, lineIndex, columnIndex)
        tiles[tileId]?.apply {
            bounds = rect
            draw(canvas)
        }
        canvas.drawRect(rect, paint)
    }

}