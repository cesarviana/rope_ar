package com.rope.ropelandia.game.bitmaptaker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import androidx.camera.core.ImageProxy
import androidx.camera.core.UseCase
import com.rope.ropelandia.capture.imagetobitmap.ImageToBitmapConverterFactory

typealias BitmapTookCallback = (Bitmap) -> Unit

abstract class BitmapTaker(context: Context, private val handler: Handler, private val bitmapTookCallback: BitmapTookCallback) {
    abstract fun startTakingImages()
    abstract fun getUseCase() : UseCase
    private val toBitmapConverterFactory = ImageToBitmapConverterFactory(context)
    @SuppressLint("UnsafeExperimentalUsageError")
    protected fun imageTaken(imageProxy: ImageProxy) {
        handler.post {
            imageProxy.image?.let {
                val bitmap = toBitmapConverterFactory.getConverter(it).convert(it)
                bitmapTookCallback.invoke(bitmap)
            }
            imageProxy.close()
        }
    }

    abstract fun stop()
}