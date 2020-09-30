package com.example.ropelandia

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import kotlin.math.cos
import kotlin.math.sin

class Mat(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs) {

    var blocks = listOf<Block>()
        set(value) {
            field = value
            program.clear()
            program.addAll(ProgramFactory.createProgram(blocks))
        }
    private val program = mutableListOf<Block>()

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSPARENT)
    }

    private val blockPaint = Paint().apply {
        color = Color.RED
    }

    private val textPaint = Paint().apply {
        textSize = 50f
        color = Color.BLUE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {

            blocks.forEach {
//                drawCircle(it.x, it.y, it.diameter, blockPaint)
//                drawText(it.angle.toString(), it.x + 20, it.y, textPaint)
            }


            drawPlaceholder(canvas)

        }
    }

    private fun drawPlaceholder(canvas: Canvas) {

        val lastBlock = program.lastOrNull()

        lastBlock?.let {
            val degrees = Math.toDegrees(it.angle.toDouble())
            canvas.rotate(degrees.toFloat(), lastBlock.x, lastBlock.y)

            VectorDrawableCompat.create(resources, R.drawable.ic_placeholder, null)
                ?.let {
                    val blockRadius = lastBlock.diameter / 2
                    val blockWidth = 140
                    val blockHeight = 100

                    val left = (lastBlock.x.toInt() - blockRadius).toInt() + blockWidth
                    val top = (lastBlock.y - blockRadius).toInt()
                    val bottom = top + blockHeight
                    val right = left + blockWidth

                    it.bounds = Rect(left, top, right, bottom)
                    it.draw(canvas)

                }
        }
    }
}

