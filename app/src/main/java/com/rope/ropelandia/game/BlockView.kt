package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.rope.ropelandia.R

class BlockView(context: Context) : View(context) {

    var highlighted: Boolean = false
    var bounds = Rect()
    var angle = 0.0f

    private val highlightIcon: Drawable =
        ResourcesCompat.getDrawable(resources, R.drawable.ic_highlight, null)!!

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if(!highlighted)
            return

        val angleDegrees = Math.toDegrees(angle.toDouble()).toFloat()
        val centerX = bounds.centerX().toFloat()
        val centerY = bounds.centerY().toFloat()

        canvas?.apply {
            rotate(angleDegrees, centerX, centerY)

           highlightIcon.let {
                it.bounds = this@BlockView.bounds
                it.draw(canvas)
            }

            rotate(-angleDegrees, centerX, centerY)
        }
    }
}