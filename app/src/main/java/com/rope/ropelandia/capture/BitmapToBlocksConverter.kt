package com.rope.ropelandia.capture

import android.graphics.Bitmap
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.BlockFactory
import com.rope.ropelandia.model.PositionBlock
import topcodes.TopCode
import topcodes.TopCodesScanner

class BitmapToBlocksConverter(
    targetHeight: Int,
    targetWidth: Int,
    private val imageQuality: ImageQuality
) {

    private val topCodesScanner = TopCodesScanner()
    private val positioner = ProjectorBlocksPositioner(targetHeight, targetWidth)

    private object Cropper {
        fun crop(bitmap: Bitmap): Bitmap {
            return if (cropRect == null)
                bitmap
            else
                Bitmap.createBitmap(
                    bitmap,
                    cropRect!!.topLeft.x.toInt(),
                    cropRect!!.topLeft.y.toInt(),
                    cropRect!!.width().toInt() / 3,
                    cropRect!!.height().toInt()
                )
        }

        var cropRect: Rectangle? = null
    }

    fun convertBitmapToBlocks(bitmap: Bitmap): List<Block> {
        val blocks = bitmap.let {
            scale(it)
        }.let {
            cropAreaToScan(it)
        }.let {
            scanTopCodes(it)
        }.let {
            convertToBlocks(it)
        }

        setupCropRect(blocks)

        return blocks.let {
            reposition(it)
        }
    }

    private fun cropAreaToScan(bitmap: Bitmap) = Cropper.crop(bitmap)

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