package com.rope.ropelandia.game.converters

import android.graphics.Bitmap
import android.util.Size
import com.rope.ropelandia.capture.*
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.BlockFactory
import com.rope.ropelandia.model.PositionBlock
import com.rope.ropelandia.usecases.perspectiverectangle.domain.entities.PerspectiveRectangle
import com.rope.ropelandia.usecases.perspectiverectangle.domain.usecases.GetPerspectiveRectangle
import com.rope.ropelandia.usecases.perspectiverectangle.domain.usecases.StorePerspectiveRectangle
import topcodes.TopCode
import topcodes.TopCodesScanner
import kotlin.math.abs

private const val ACCEPTABLE_VARIATION = 10

class BitmapToBlocksConverter(
    private val screenSize: Size,
    private val getPerspectiveRectangle: GetPerspectiveRectangle,
    private val storePerspectiveRectangle: StorePerspectiveRectangle
) {

    private var homographyMatrix: HomographyMatrix? = null
    private val topCodesScanner by lazy { TopCodesScanner() }
    private val positioner = ProjectorBlocksPositioner()

    private var lastBlocks = listOf<Block>()
    private var lastNumberOfBlocks = 0

    private val targetRectangle by lazy {
        val right = screenSize.width * 1.05
        val bottom = screenSize.height * 0.95
        Rectangle(0.0, 0.0, right, bottom)
    }

    fun convertBitmapToBlocks(bitmap: Bitmap, forceCallback: Boolean, callback: (List<Block>) -> Unit) {
        val blocks = bitmap.let {
            scanTopCodes(increaseSize(it))
        }.let {
            convertToBlocks(it)
        }.let {
            storeCalibrationData(it)
            it
        }.let {
            reposition(it)
        }

        val maybeWrongDetection = toMuchVariationInBlocksNumber(blocks, lastNumberOfBlocks)

        if (maybeWrongDetection) {
            lastNumberOfBlocks = blocks.size
            return
        }

        if(blocksMoved(blocks, lastBlocks) || forceCallback) {
            lastBlocks = blocks
            callback(blocks)
        }
    }

    private fun increaseSize(it: Bitmap): Bitmap {
        val scale = 1.5
        val w = (it.width * scale).toInt()
        val h = (it.height * scale).toInt()
        return Bitmap.createScaledBitmap(it, w, h, true)
    }

    private fun toMuchVariationInBlocksNumber(
        blocks: List<Block>,
        lastNumberOfBlocks: Int
    ) = abs(lastNumberOfBlocks - blocks.size) > 2

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

    private fun storeCalibrationData(it: List<Block>) {
        val calibrationPoints = it.filterIsInstance<PositionBlock>().map { block ->
            Point(block.centerX.toDouble(), block.centerY.toDouble())
        }
        if (calibrationPoints.size == 4) {
            val perspectiveRectangle = PerspectiveRectangle.createFromPoints(calibrationPoints)
            updateHomography(perspectiveRectangle)
            storePerspectiveRectangle.execute(perspectiveRectangle)
        }
    }

    private fun updateHomography(perspectiveRectangle: PerspectiveRectangle) {
        homographyMatrix = HomographyMatrixCalculator.calculate(
            perspectiveRectangle, targetRectangle
        )
    }

    private fun reposition(blocks: List<Block>): List<Block> {
        positioner.apply {
            homographyMatrix = getHomographyMatrix()
            return reposition(blocks)
        }
    }

    private fun getHomographyMatrix(): HomographyMatrix {
        if (this.homographyMatrix == null) {
            getPerspectiveRectangle.execute().let { perspectiveRectangle ->
                updateHomography(perspectiveRectangle)
            }
        }
        return this.homographyMatrix!!
    }
}