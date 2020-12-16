package com.rope.ropelandia.game

import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import com.rope.ropelandia.capture.BitmapToBlocksConverter
import java.io.File

class ImageSavedCallback(private val imageFile: File) : ImageCapture.OnImageSavedCallback {

    private lateinit var onFoundBlocks: (List<Block>) -> Unit
    private val bitmapToBlocksConverter = BitmapToBlocksConverter(720, 1280)

    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
        getBitmap().let {
            bitmapToBlocksConverter.convertBitmapToBlocks(it)
        }.let {
            onFoundBlocks(it)
        }
    }

    private fun getBitmap() = BitmapFactory.decodeFile(imageFile.toString())

    override fun onError(exception: ImageCaptureException) {
        Log.e("ImageSavedCallback", exception.message, exception)
    }

    fun onFoundBlocks(function: (List<Block>) -> Unit) {
        this.onFoundBlocks = function
    }
}