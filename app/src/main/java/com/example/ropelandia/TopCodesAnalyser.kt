package com.example.ropelandia

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import topcodes.TopCodesScanner
import topcodes.TopCode

class TopCodesAnalyser(
    private val cameraImageConverter: CameraImageConverter,
    private val topCodesScanner: TopCodesScanner,
    private val listener: (topCodes: List<TopCode>) -> Unit
) :
    ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        try {
            image.image?.let {
                cameraImageConverter.convert(it)
            }?.let { bitmap ->
                topCodesScanner.searchTopCodes(bitmap)
            }?.let { topCodes ->
                listener(topCodes)
            }
        } finally {
            image.close()
        }
    }

}