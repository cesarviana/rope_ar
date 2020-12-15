package com.rope.ropelandia.game

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.io.File

class ImageSavedCallback(private val imageFile: File) : ImageCapture.OnImageSavedCallback {

    private lateinit var onBitmap: (Bitmap) -> Unit

    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) =
        onBitmap(getBitmap())

    private fun getBitmap() = BitmapFactory.decodeFile(imageFile.toString())

    override fun onError(exception: ImageCaptureException) {
        Log.e("ImageSavedCallback", exception.message, exception)
    }

    fun onBitmap(function: (Bitmap) -> Unit) {
        this.onBitmap = function
    }
}