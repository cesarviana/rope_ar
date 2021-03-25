package com.rope.ropelandia.game.converters

import android.graphics.Bitmap
import android.util.Size
import com.rope.ropelandia.capture.ProjectorBlocksPositioner
import com.rope.ropelandia.game.converters.TopCodeToBlockConverter
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.BlockFactory
import topcodes.TopCode
import topcodes.TopCodesScanner
import kotlin.math.abs

private const val ACCEPTABLE_VARIATION = 10

class BitmapToBlocksConverter(screenSize: Size) {

    private val topCodesScanner by lazy { TopCodesScanner() }
    private val positioner by lazy {
        ProjectorBlocksPositioner(
            (screenSize.height * 0.95).toInt(),
            (screenSize.width * 1.05).toInt()
        )
    }

    private var lastBlocks = listOf<Block>()
    private var lastNumberOfBlocks = 0

    fun convertBitmapToBlocks(bitmap: Bitmap): List<Block> {
        val blocks = bitmap.let {
            val scale = 1.5
            val w = (it.width * scale).toInt()
            val h = (it.height * scale).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(it, w, h, true)
            scanTopCodes(scaledBitmap)
        }.let {
            convertToBlocks(it)
        }.let {
            reposition(it)
        }
        if(toMuchVariation(blocks, lastNumberOfBlocks)){
            lastNumberOfBlocks = blocks.size
            return lastBlocks
        }
        if (blocksMoved(blocks, lastBlocks)) {
            lastBlocks = blocks
        }
        return lastBlocks
    }

    private fun toMuchVariation(blocks: List<Block>, lastNumberOfBlocks: Int): Boolean {
        return ( abs(lastNumberOfBlocks - blocks.size)  > 2 )
    }

    private fun blocksMoved(blocks: List<Block>, lastBlocks: List<Block>): Boolean {
        if (blocks.size != lastBlocks.size) {
            return true
        }
        val comparator = Comparator { block1: Block, block2: Block ->
            ((block1.centerX + block1.centerY) - (block2.centerX + block2.centerY)).toInt()
        }
        val lastBlocksSorted = lastBlocks.sortedWith(comparator)
        blocks.sortedWith(comparator).forEachIndexed { index, block ->
            val lastBlock = lastBlocksSorted[index]
            val xDiff = abs(lastBlock.centerX - block.centerX)
            if (xDiff > ACCEPTABLE_VARIATION)
                return true
        }
        return false
    }

    private fun scanTopCodes(it: Bitmap) = topCodesScanner.searchTopCodes(it)

    private fun convertToBlocks(topCodes: List<TopCode>) = topCodes.map {
        val blockClass = TopCodeToBlockConverter.map(it.code)
        BlockFactory.createBlock(
            blockClass, it.centerX, it.centerY, it.diameter, it.angleInRadians
        )
    }

    private fun reposition(blocks: List<Block>) = positioner.reposition(blocks)
}