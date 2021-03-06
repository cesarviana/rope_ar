package com.rope.ropelandia.game

import android.annotation.SuppressLint
import android.os.Handler
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.rope.ropelandia.capture.BitmapToBlocksConverter
import com.rope.ropelandia.capture.ImageToBitmapConverter
import com.rope.ropelandia.model.Block
import java.util.concurrent.Executors

typealias BlocksFoundListener = (List<Block>) -> Unit

class MyImageAnalyser(
    private val imageToBitmapConverter: ImageToBitmapConverter,
    private val bitmapToBlocksConverter: BitmapToBlocksConverter,
    private val handler: Handler,
    val listener: BlocksFoundListener
) :
    ImageAnalysis.Analyzer {

    private val executorService by lazy { Executors.newSingleThreadExecutor() }

    override fun analyze(image: ImageProxy) {
        executorService.submit {
            try {
                val blocks = findBlocks(image)
                handler.post { listener(blocks); }
            } finally {
                image.close()
            }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun findBlocks(image: ImageProxy): List<Block> {
        val bitmap = image.image?.let { imageToBitmapConverter.convertToBitmap(it) }
        return bitmapToBlocksConverter.convertBitmapToBlocks(bitmap!!)
    }

}
