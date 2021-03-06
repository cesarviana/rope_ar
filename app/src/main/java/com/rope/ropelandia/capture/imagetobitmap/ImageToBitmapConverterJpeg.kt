package com.rope.ropelandia.capture.imagetobitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image

class ImageToBitmapConverterJpeg : ImageToBitmapConverter {
    override fun convert(image: Image): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity()).also { buffer.get(it) }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}