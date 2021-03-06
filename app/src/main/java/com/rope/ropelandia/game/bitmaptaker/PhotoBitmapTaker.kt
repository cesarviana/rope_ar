package com.rope.ropelandia.game.bitmaptaker

import android.content.Context
import android.os.Handler
import android.util.Size
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.UseCase
import java.util.concurrent.Executors

class PhotoBitmapTaker(context: Context, handler: Handler, bitmapTookCallback: BitmapTookCallback) : BitmapTaker(
    context,
    handler,
    bitmapTookCallback
) {

    private val width = context.resources.displayMetrics.widthPixels
    private val height = context.resources.displayMetrics.heightPixels

    private val imageCapture = ImageCapture.Builder()
        .setTargetResolution(Size(width, height))
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()

    override fun startTakingImages() {
        val executor = Executors.newSingleThreadExecutor()
        val callback = object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                imageTaken(image)
                imageCapture.takePicture(executor, this)
            }
            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
            }
        }
        imageCapture.takePicture(executor, callback)
    }

    override fun getUseCase(): UseCase = imageCapture

}
