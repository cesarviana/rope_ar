package com.rope.ropelandia.game

import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import com.rope.ropelandia.capture.BitmapToBlocksConverter
import com.rope.ropelandia.capture.ImageQuality
import java.io.File

data class ImageProcessingConfig(val imageFile: File, val imageQuality: ImageQuality)

class ImageSavedCallback(private val imageProcessingConfig: ImageProcessingConfig) : ImageCapture.OnImageSavedCallback {

    private lateinit var onFoundBlocks: (List<Block>) -> Unit
    private val bitmapToBlocksConverter = BitmapToBlocksConverter(720, 1280, imageProcessingConfig.imageQuality)

    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
        getBitmap().let {
            bitmapToBlocksConverter.convertBitmapToBlocks(it)
        }.let {
            onFoundBlocks(it)
        }
    }

    private fun getBitmap() = BitmapFactory.decodeFile(imageProcessingConfig.imageFile.toString())

    override fun onError(exception: ImageCaptureException) {
        Log.e("ImageSavedCallback", exception.message, exception)
    }

    fun onFoundBlocks(function: (List<Block>) -> Unit) {
        this.onFoundBlocks = function
    }
}