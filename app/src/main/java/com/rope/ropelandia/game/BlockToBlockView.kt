package com.rope.ropelandia.game

import android.content.Context
import android.graphics.Rect
import com.rope.ropelandia.model.Block

object BlockToBlockView {
    fun convert(context: Context, block: Block) = BlockView(context).apply {
        bounds = Rect(
            block.left.toInt(),
            block.top.toInt(),
            block.right.toInt(),
            block.bottom.toInt()
        )
        angle = block.angle
    }
}