package com.rope.ropelandia

import android.graphics.Bitmap
import topcodes.TopCode
import topcodes.TopCodesScanner

class BitmapToBlocksConverter(targetHeight: Int, targetWidth: Int) {

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
        val scale = .6
        val width = (bitmap.width * scale).toInt()
        val height = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun scanTopCodes(it: Bitmap) = topCodesScanner.searchTopCodes(it)

    private fun convertToBlocks(topCodes: List<TopCode>) = topCodes.map {
        val blockClass = TopCodeToClassMapper.map(it.code)

        val pastedPaperErrorInRadians = 1.5708f

        val angle = it.angleInRadians - pastedPaperErrorInRadians

        BlockFactory.createBlock(
            blockClass, it.centerX, it.centerY, it.diameter, angle
        )
    }

    private fun reposition(blocks: List<Block>) = positioner.reposition(blocks)
}