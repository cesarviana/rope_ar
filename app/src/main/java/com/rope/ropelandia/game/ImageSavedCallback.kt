package com.rope.ropelandia.game

import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import com.rope.ropelandia.capture.BitmapToBlocksConverter
import com.rope.ropelandia.capture.ImageQuality
import com.rope.ropelandia.model.Block
import java.io.File

data class ImageProcessingConfig(val imageFile: File, val imageQuality: ImageQuality)

class ImageSavedCallback(private val imageProcessingConfig: ImageProcessingConfig) : ImageCapture.OnImageSavedCallback {

    private var onFoundBlocks: ((Array<Block>) -> Unit)? = null
    private var onFoundBlocksException: ((Exception) -> Unit)? = null
    private val bitmapToBlocksConverter = BitmapToBlocksConverter(720, 1280)

    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
        try {
            val bitmap = getBitmap()
            val blocks = bitmapToBlocksConverter.convertBitmapToBlocks(bitmap)
            onFoundBlocks?.invoke(blocks)
        } catch (e: Exception) {
            onFoundBlocksException?.invoke(e)
        }
    }

    private fun getBitmap() = BitmapFactory.decodeFile(imageProcessingConfig.imageFile.toString())

    override fun onError(exception: ImageCaptureException) {
        Log.e("ImageSavedCallback", exception.message, exception)
    }

    fun onFoundBlocks(function: (Array<Block>) -> Unit) {
        this.onFoundBlocks = function
    }

    fun onFoundBlocksException(function: (Exception) -> Unit) {
        this.onFoundBlocksException = function
    }
}