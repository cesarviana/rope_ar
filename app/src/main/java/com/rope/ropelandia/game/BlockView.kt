package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.rope.ropelandia.R

class BlockView(context: Context) : View(context) {

    var highlighted: Boolean = false
    var bounds = Rect()
    var angle = 0.0f

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if(!highlighted)
            return

        val angleDegrees = Math.toDegrees(angle.toDouble()).toFloat()
        val centerX = bounds.centerX().toFloat()
        val centerY = bounds.centerY().toFloat()

        canvas?.apply {
            rotate(angleDegrees, centerX, centerY)

            ResourcesCompat.getDrawable(resources, R.drawable.ic_highlight, null)?.let {
                it.bounds = this@BlockView.bounds
                it.draw(canvas)
            }

            rotate(-angleDegrees, centerX, centerY)
        }
    }
}