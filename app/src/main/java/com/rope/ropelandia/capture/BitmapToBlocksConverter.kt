package com.rope.ropelandia.capture

import android.graphics.Bitmap
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.BlockFactory
import topcodes.TopCode
import topcodes.TopCodesScanner
import kotlin.math.cos
import kotlin.math.sin

class BitmapToBlocksConverter(
    targetHeight: Int,
    targetWidth: Int,
    private val imageQuality: ImageQuality
) {

    private val topCodesScanner = TopCodesScanner()
    private val positioner = ProjectorBlocksPositioner(targetHeight, targetWidth)

    fun convertBitmapToBlocks(bitmap: Bitmap): List<Block> {
        return bitmap.let {
            scale(it)
        }.let {
            scanTopCodes(it)
        }.let {
            convertToBlocks(it)
        }.let {
            reposition(it)
        }
    }

    private fun scale(bitmap: Bitmap): Bitmap {
        val scale = imageQuality.floatValue()
        val width = (bitmap.width * scale).toInt()
        val height = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun scanTopCodes(it: Bitmap): List<TopCode> {
        topCodesScanner.setMaxCodeDiameter(200)
        return topCodesScanner.searchTopCodes(it)
    }

    private fun convertToBlocks(topCodes: List<TopCode>) = topCodes.map {
        val blockClass = TopCodeToClassMapper.map(it.code)

        BlockFactory.createBlock(
            blockClass, it.centerX, it.centerY, it.diameter, it.angleInRadians
        )
    }

    private fun reposition(blocks: List<Block>) = positioner.reposition(blocks)
}