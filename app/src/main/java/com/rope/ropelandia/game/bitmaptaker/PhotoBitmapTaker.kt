package com.rope.ropelandia.game.bitmaptaker

import android.content.Context
import android.os.Handler
import android.util.Size
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.UseCase
import java.util.concurrent.Executors

class PhotoBitmapTaker(context: Context, handler: Handler, bitmapTookCallback: BitmapTookCallback) :
    BitmapTaker(
        context,
        handler,
        bitmapTookCallback
    ) {

    private val imageCapture by lazy {
        val width = context.resources.displayMetrics.widthPixels * 2
        val height = context.resources.displayMetrics.heightPixels * 2
        ImageCapture.Builder()
            .setTargetResolution(Size(width, height))
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    private val executor by lazy {
        Executors.newSingleThreadExecutor()
    }

    override fun startTakingImages() {
        val callback = object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                imageTaken(image)
                imageCapture.takePicture(executor, this)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                imageCapture.takePicture(executor, this)
            }
        }
        imageCapture.takePicture(executor, callback)
    }

    override fun stop() = executor.shutdown()

    override fun getUseCase(): UseCase = imageCapture

}
