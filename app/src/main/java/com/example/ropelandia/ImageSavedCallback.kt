package com.example.ropelandia

import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.io.File

class ImageSavedCallback(private val imageFile: File, private val mat: Mat) :
    ImageCapture.OnImageSavedCallback {

    private val bitmapToBlocksConverter = BitmapToBlocksConverter(mat.height, mat.width)

    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) =
        getBitmap().let {
            bitmapToBlocksConverter.convertBitmapToBlocks(it)
        }.let {
            updateView(it)
        }

    private fun getBitmap() = BitmapFactory.decodeFile(imageFile.toString())

    private fun updateView(it: List<Block>) {
        mat.blocks = it
        mat.invalidate()
    }

    override fun onError(exception: ImageCaptureException) {
        Log.e("ImageSavedCallback", exception.message, exception)
    }
}