package com.example.ropelandia

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
        val radians = it.angleInRadians.toDouble()

        val pastedTopCodeAngleError = 90

        val degrees = (Math.toDegrees(radians) - pastedTopCodeAngleError).let { degrees ->
            if (degrees < 0)
                degrees + 360
            else
                degrees
        }

        BlockFactory.createBlock(
            blockClass, it.centerX, it.centerY, it.diameter, degrees.toFloat()
        )
    }

    private fun reposition(blocks: List<Block>) = positioner.reposition(blocks)
}