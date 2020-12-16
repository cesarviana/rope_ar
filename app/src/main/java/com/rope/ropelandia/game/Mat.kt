package com.rope.ropelandia.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.rope.ropelandia.capture.ProgramFactory
import com.rope.ropelandia.R

class Mat(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs) {

    var blocks = listOf<Block>()
        set(value) {
            field = value
            program.clear()
            program.addAll(ProgramFactory.findSequence(blocks))
        }
    private val program = mutableListOf<Block>()

    init {
        setWillNotDraw(false)
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSPARENT)
//        setBackgroundColor(Color.WHITE)
    }

    private val blockPaint = Paint().apply {
        color = Color.RED
    }

    private val textPaint = Paint().apply {
        textSize = 50f
        color = Color.BLUE
    }

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.GREEN
    }

    private val white = Paint().apply {
        color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {

            drawRect(0f, 0f, width.toFloat(), height.toFloat(), white)

            blocks.forEach {
                drawCircle(it.x, it.y, it.diameter, blockPaint)
                drawText(it.angle.toString(), it.x + 20, it.y, textPaint)
            }
//            drawRect(0f, 0f, width.toFloat() - 3, height.toFloat() - 3, borderPaint)
//            drawPlaceholder(canvas)

        }
    }

    private fun drawPlaceholder(canvas: Canvas) {

        val baseBlock = program.find { it is StartBlock }

        baseBlock?.let {
            val degrees = Math.toDegrees(it.angle.toDouble())
            canvas.rotate(degrees.toFloat(), baseBlock.x, baseBlock.y)

            VectorDrawableCompat.create(resources, R.drawable.ic_placeholder, null)
                ?.let { drawable ->
                    val blockRadius = baseBlock.diameter / 2
                    val blockWidth = 140
                    val blockHeight = 100

                    val left = (baseBlock.x.toInt() - blockRadius).toInt() + blockWidth
                    val top = (baseBlock.y - blockRadius).toInt()
                    val bottom = top + blockHeight
                    val right = left + blockWidth

                    drawable.bounds = Rect(left, top, right, bottom)
                    drawable.draw(canvas)

                    program.forEach { _ ->
                        drawable.bounds.left += blockWidth
                        drawable.bounds.right = drawable.bounds.left + blockWidth
                        drawable.alpha = (drawable.alpha * 0.8).toInt()
                        drawable.draw(canvas)
                    }

                }
        }
    }
}

