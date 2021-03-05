package com.rope.ropelandia.capture

import android.graphics.Bitmap
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.BlockFactory
import com.rope.ropelandia.model.PositionBlock
import topcodes.TopCode
import topcodes.TopCodesScanner

class BitmapToBlocksConverter(
    targetHeight: Int = 720,
    targetWidth: Int = 1080
) {

    private val topCodesScanner = TopCodesScanner()
    private val positioner = ProjectorBlocksPositioner(targetHeight, targetWidth)

    private object Cropper {
        fun crop(bitmap: Bitmap): Bitmap {
            val cropMargin = 0.2
            val marginWidth = (bitmap.width * cropMargin).toInt()
            val marginHeight = (bitmap.height * cropMargin).toInt()
            return Bitmap.createBitmap(
                bitmap,
                marginWidth,
                marginHeight,
                bitmap.width - marginWidth,
                bitmap.height - marginHeight
            )
        }

        var cropRect: Rectangle? = null
    }

    fun convertBitmapToBlocks(bitmap: Bitmap): Array<Block> {
        val blocks = bitmap.let {
//            scale(it)
//        }.let {
//            cropAreaToScan(it)
//        }.let {
            scanTopCodes(it)
        }.let {
            convertToBlocks(it)
        }

        return blocks.let {
            reposition(it)
        }.toTypedArray()
    }

    private fun cropAreaToScan(bitmap: Bitmap) = Cropper.crop(bitmap)

    private fun scale(bitmap: Bitmap): Bitmap {
        val scale = 0.55 //imageQuality.floatValue()
        val width = (bitmap.width * scale).toInt()
        val height = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun scanTopCodes(it: Bitmap) = topCodesScanner.searchTopCodes(it)

    private fun convertToBlocks(topCodes: Array<TopCode>) = topCodes.map {
        val blockClass = TopCodeToClassMapper.map(it.code)

        BlockFactory.createBlock(
            blockClass, it.centerX, it.centerY, it.diameter, it.angleInRadians
        )
    }

    private fun setupCropRect(blocks: List<Block>) {
        val positionBlocks = blocks.filterIsInstance<PositionBlock>()
        val sufficientRectPoints = positionBlocks.size == 4
        if (sufficientRectPoints) {
            val points = positionBlocks.map { Point(it.centerX.toDouble(), it.centerY.toDouble()) }
            val rectangle = RectangleFinder().adjustRectangle(points)
            Cropper.cropRect = rectangle
        }
    }

    private fun reposition(blocks: List<Block>) = positioner.reposition(blocks)
}