package topcodes

import android.graphics.Bitmap

open class TopCodesScanner {

    fun searchTopCodes(bitmap: Bitmap): List<TopCode> {
        val imageWidth = bitmap.width
        val imageHeight = bitmap.height
        val imageData = IntArray(imageWidth * imageHeight)
        bitmap.getPixels(imageData, 0, imageWidth, 0, 0, imageWidth, imageHeight)
        return searchTopCodesNative(imageWidth, imageHeight, imageData).toList()
    }

    private external fun searchTopCodesNative(imageWidth: Int, imageHeight: Int, imageData: IntArray) : Array<TopCode>
}