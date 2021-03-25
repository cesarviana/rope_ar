package com.rope.ropelandia.game.converters

import android.content.Context
import android.graphics.Rect
import com.rope.ropelandia.game.views.BlockView
import com.rope.ropelandia.model.Block

object BlockToBlockViewConverter {
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