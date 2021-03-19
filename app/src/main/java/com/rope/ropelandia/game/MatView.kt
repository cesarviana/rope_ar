package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.rope.ropelandia.R
import com.rope.ropelandia.game.tiles.Tile

class MatView(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet) {

    private var assets = listOf<Tile>()
    private val floor = ResourcesCompat.getDrawable(context.resources, R.drawable.floor, null)!!

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
//            drawFloor(canvas)
            drawElements(canvas)
        }
    }

//    private fun drawFloor(canvas: Canvas) {
//        assets.map {
//            it.square
//        }.toSet().forEach {
//            drawTile(canvas, it, floor)
//        }
//    }

    private fun drawElements(canvas: Canvas) {
        assets.forEach {
            it.draw(canvas)
        }
    }

    fun updateMat(tiles: List<Tile>, lines: Int, columns: Int) {
        this.assets = tiles
    }

}