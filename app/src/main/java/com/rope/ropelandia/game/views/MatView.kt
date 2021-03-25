package com.rope.ropelandia.game.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.rope.ropelandia.R
import com.rope.ropelandia.game.tiles.Tile

class MatView(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet) {

    private var tiles = listOf<Tile>()
    private val floor = ResourcesCompat.getDrawable(context.resources, R.drawable.floor, null)!!

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawFloor(canvas)
            drawElements(canvas)
        }
    }

    private fun drawFloor(canvas: Canvas) {
        tiles.forEach { tile ->
            floor.apply {
                val size = tile.squareSize
                val top = tile.square.line * size
                val left = tile.square.column * size
                val right = left + size
                val bottom = top + size
                bounds = Rect(left, top, right, bottom)
                draw(canvas)
            }
        }
    }

    private fun drawElements(canvas: Canvas) {
        tiles.forEach {
            it.draw(canvas)
        }
    }

    fun updateMat(tiles: List<Tile>) {
        this.tiles = tiles
    }

}