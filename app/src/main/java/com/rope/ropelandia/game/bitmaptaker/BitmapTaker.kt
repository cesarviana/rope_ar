package com.rope.ropelandia.game.bitmaptaker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.core.UseCase
import com.rope.ropelandia.capture.imagetobitmap.ImageToBitmapConverterFactory
import java.util.concurrent.ExecutorService

abstract class BitmapTaker(
    context: Context,
    private val handler: Handler,
    protected val executor: ExecutorService,
    private val bitmapTookCallback: BitmapTookCallback
) {
    abstract fun startTakingImages()
    abstract fun getUseCase(): UseCase
    private val toBitmapConverterFactory = ImageToBitmapConverterFactory(context)
    private var onStopCallback: (() -> Unit)? = null

    @SuppressLint("UnsafeExperimentalUsageError")
    protected fun imageTaken(imageProxy: ImageProxy) {
        imageProxy.image?.let {
            try {
                val bitmap = toBitmapConverterFactory.getConverter(it).convert(it)
                imageProxy.close()
                handler.post {
                    bitmapTookCallback.onBitmap(bitmap)
                }
            } catch (e: Exception) {
                handler.post {
                    bitmapTookCallback.onError(e)
                }
            } finally {
                imageProxy.close()
            }
        }
    }

    interface BitmapTookCallback {
        fun onBitmap(bitmap: Bitmap)
        fun onError(e: Exception)
    }

    protected fun onError(e: Exception) = bitmapTookCallback.onError(e)

    fun stop() {
        try {
            executor.shutdown()
        } catch (e: java.lang.Exception) {
            e.message?.let { Log.e("BITMAP_TAKER", it) }
        } finally {
            onStopCallback?.invoke()
        }
    }

    fun onStopping(function: () -> Unit) {
        this.onStopCallback = function
    }
}