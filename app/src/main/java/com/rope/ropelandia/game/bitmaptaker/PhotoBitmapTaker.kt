package com.rope.ropelandia.game.bitmaptaker

import android.content.Context
import android.util.Size
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.UseCase
import java.util.concurrent.ExecutorService

class PhotoBitmapTaker(context: Context, executor: ExecutorService, bitmapTookCallback: BitmapTookCallback) :
    BitmapTaker(
        context,
        executor,
        bitmapTookCallback
    ) {

    private val imageCapture by lazy {
        val width = context.resources.displayMetrics.widthPixels * 1.7
        val height = context.resources.displayMetrics.heightPixels * 1.7
        ImageCapture.Builder()
            .setTargetResolution(Size(width.toInt(), height.toInt()))
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }

    override fun startTakingImages() {
        val callback = object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                imageTaken(image)
                takePictureIfExecutorRunning(this)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                this@PhotoBitmapTaker.onError(exception)
            }
        }
        takePictureIfExecutorRunning(callback)
    }

    private fun takePictureIfExecutorRunning(callback: ImageCapture.OnImageCapturedCallback) {
        if (!executor.isShutdown)
            imageCapture.takePicture(executor, callback)
    }

    override fun getUseCase(): UseCase = imageCapture

}
