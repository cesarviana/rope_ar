package com.rope.ropelandia.capture.imagetobitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image

interface ImageToBitmapConverter {
    fun convert(image: Image) : Bitmap
}

class ImageToBitmapConverterFactory(private val context: Context) {

    private val jpegConverter by lazy { ImageToBitmapConverterJpeg() }
    private val yuvConverter by lazy { ImageToBitmapConverterYUV420_888(context) }

    fun getConverter(image: Image) : ImageToBitmapConverter {
        return when (image.format){
            ImageFormat.JPEG -> jpegConverter
            ImageFormat.YUV_420_888 -> yuvConverter
            else -> throw UnsupportedOperationException("No converter implemented for the format [${image.format}]")
        }
    }
}
