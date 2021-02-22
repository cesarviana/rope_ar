package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout

class GameView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {

//    var matView = MatView(context, null)
//    set(value) {
//        field = value
//        matView.minimumHeight = height
//        invalidate()
//    }
    var blocksViews: List<BlockView> = listOf()

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
        setBackgroundColor(Color.BLACK)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.apply {
            blocksViews.forEach {
                it.draw(canvas)
            }
//            matView.draw(canvas)
        }
    }

    fun hideHighlight() {
        blocksViews.forEach {
            it.highlighted = false
        }
        updateDraw()
    }

    private fun updateDraw() {
        val canvas = holder.lockCanvas()
        draw(canvas)
        holder.unlockCanvasAndPost(canvas)
    }

    fun highlight(actionIndex: Int) {
        if(blocksViews.size > actionIndex) {
            val blockView = blocksViews[actionIndex]
            blockView.highlighted = true
            updateDraw()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

}

