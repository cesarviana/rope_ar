package com.rope.ropelandia.game.bitmaptaker

import android.content.Context
import android.os.Handler
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.UseCase
import java.util.concurrent.Executors

class StreamBitmapTaker(context: Context, handler: Handler, bitmapTookCallback: BitmapTookCallback) :
    BitmapTaker(context, handler, bitmapTookCallback) {

    private val executor = Executors.newSingleThreadExecutor()

    private val myImageAnalyser = ImageAnalysis.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also {
            it.setAnalyzer(
                executor
            ) { image ->
                imageTaken(image)
            }
        }

    // starts automatically
    override fun startTakingImages() {}

    override fun getUseCase(): UseCase = myImageAnalyser

    override fun stop() = executor.shutdown()
}