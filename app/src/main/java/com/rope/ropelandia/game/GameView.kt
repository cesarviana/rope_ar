package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    var matView: MatView = MatView(context, null)

    var blocksViews: List<BlockView> = listOf()

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        clear(canvas)
        canvas?.apply {
            matView.draw(canvas)
            blocksViews.forEach {
                it.draw(canvas)
            }
        }
    }

    private fun clear(canvas: Canvas?) {
        canvas?.drawColor(0, PorterDuff.Mode.CLEAR)
    }

    fun highlight(actionIndex: Int) {
        if(blocksViews.size > actionIndex) {
            val blockView = blocksViews[actionIndex]
            blockView.highlighted = true
            updateDraw()
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
        try {
            draw(canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

}

