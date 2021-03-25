package com.rope.ropelandia.game.bitmaptaker

import android.content.Context
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.UseCase
import java.util.concurrent.ExecutorService

class VideoBitmapTaker(
    context: Context,
    executor: ExecutorService,
    bitmapTookCallback: BitmapTookCallback
) :
    BitmapTaker(context, executor, bitmapTookCallback) {

    private val myImageAnalyser = ImageAnalysis.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setBackgroundExecutor(executor)
        .build()
        .also {
            it.setAnalyzer(
                this.executor
            ) { image ->
                imageTaken(image)
            }
        }

    // starts automatically
    override fun startTakingImages() {}

    override fun getUseCase(): UseCase = myImageAnalyser
}