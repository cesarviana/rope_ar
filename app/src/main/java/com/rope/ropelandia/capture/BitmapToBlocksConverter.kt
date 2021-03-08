package com.rope.ropelandia.capture

import android.graphics.Bitmap
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.BlockFactory
import topcodes.TopCode
import topcodes.TopCodesScanner

class BitmapToBlocksConverter(
    targetHeight: Int,
    targetWidth: Int
) {

    private val topCodesScanner by lazy { TopCodesScanner() }
    private val positioner by lazy { ProjectorBlocksPositioner(targetHeight, targetWidth) }

    fun convertBitmapToBlocks(bitmap: Bitmap): List<Block> {
        return bitmap.let {
            scanTopCodes(it)
        }.let {
            convertToBlocks(it)
        }.let {
            reposition(it)
        }
    }

    private fun scanTopCodes(it: Bitmap) = topCodesScanner.searchTopCodes(it)

    private fun convertToBlocks(topCodes: List<TopCode>) = topCodes.map {
        val blockClass = TopCodeToClassMapper.map(it.code)

        BlockFactory.createBlock(
            blockClass, it.centerX, it.centerY, it.diameter, it.angleInRadians
        )
    }

    private fun reposition(blocks: List<Block>) = positioner.reposition(blocks)
}