package topcodes

import android.graphics.Bitmap
import android.util.Log

class TopCodesScanner {

    companion object {
        init {
            System.loadLibrary("native-lib");
        }
    }

    fun searchTopCodes(bitmap: Bitmap): List<TopCode> {
        val imageWidth = bitmap.width
        val imageHeight = bitmap.height
        val imageData = IntArray(imageWidth * imageHeight)
        bitmap.getPixels(imageData, 0, imageWidth, 0, 0, imageWidth, imageHeight)
        bitmap.recycle()
        val topCodes = searchTopCodesNative(imageWidth, imageHeight, imageData).toList()
        Log.d("TopCodesScanner", "Blocks found: ${topCodes.size}")
        val codes = topCodes.joinToString { it.code.toString() }
        Log.d("TopCodesScanner" ,"Blocks codes: $codes")
        return topCodes
    }

    private external fun searchTopCodesNative(imageWidth: Int, imageHeight: Int, imageData: IntArray) : Array<TopCode>
}