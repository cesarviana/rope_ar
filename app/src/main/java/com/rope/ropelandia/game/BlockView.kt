package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.rope.ropelandia.R
import com.rope.ropelandia.model.Block

/**
 * Decorate a block to allow drawing.
 */
class BlockView(context: Context, val block: Block) : View(context) {

    var highlighted: Boolean = false

    private val blockRect = Rect(block.left.toInt(),
                                 block.top.toInt(),
                                 block.right.toInt(),
                                 block.bottom.toInt())

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if(!highlighted)
            return

        val angle = block.angle.toDouble()

        val angleDegrees = Math.toDegrees(angle).toFloat()

        canvas?.apply {
            rotate(angleDegrees, block.centerX, block.centerY)

            ResourcesCompat.getDrawable(resources, R.drawable.ic_placeholder, null)?.apply {
                bounds = blockRect
                draw(canvas)
            }

            rotate(-angleDegrees, block.centerX, block.centerY)
        }
    }
}