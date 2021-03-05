package com.rope.ropelandia.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.os.Handler
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.renderscript.*
import com.rope.ropelandia.capture.BitmapToBlocksConverter
import com.rope.ropelandia.model.Block
import com.viana.soundprogramming.ScriptC_yuv4208888
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.Executors

typealias ImageAnalyserListener = (List<Block>) -> Unit

class MyImageAnalyser(
    context: Context,
    private val handler: Handler,
    val listener: ImageAnalyserListener
) :
    ImageAnalysis.Analyzer {

    private val renderScript by lazy { RenderScript.create(context) }

    private val mYuv420 by lazy { ScriptC_yuv4208888(renderScript) }
    private val bitmapToBlocksConverter by lazy { BitmapToBlocksConverter() }

    private val executorService by lazy { Executors.newSingleThreadExecutor() }

    override fun analyze(image: ImageProxy) {
        executorService.submit {
            try {
                val blocks = findBlocks(image)
                handler.post { listener(blocks); }
            } finally {
                image.close()
            }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun findBlocks(image: ImageProxy): List<Block> {
        val bitmap = image.image?.let { readImage(it) }
        return bitmapToBlocksConverter.convertBitmapToBlocks(bitmap!!)
    }

    private fun readImage(image: Image): Bitmap {
        return yuv420888toRGB(image, image.width, image.height)
    }

    private fun yuv420888toRGB(image: Image, width: Int, height: Int): Bitmap {

        // Get the three image planes
        val planes = image.planes
        var buffer = planes[0].buffer
        val y = ByteArray(buffer.remaining())
        buffer.get(y)

        buffer = planes[1].buffer
        val u = ByteArray(buffer.remaining())
        buffer.get(u)

        buffer = planes[2].buffer
        val v = ByteArray(buffer.remaining())
        buffer.get(v)

        // get the relevant RowStrides and PixelStrides
        // (we know from documentation that PixelStride is 1 for y)
        val yRowStride = planes[0].rowStride
        val uvRowStride =
            planes[1].rowStride  // we know from   documentation that RowStride is the same for u and v.
        val uvPixelStride =
            planes[1].pixelStride  // we know from   documentation that PixelStride is the same for u and v.

        // Y,U,V are defined as global allocations, the out-Allocation is the Bitmap.
        // Note also that uAlloc and vAlloc are 1-dimensional while yAlloc is 2-dimensional.
        val typeUcharY = Type.Builder(renderScript, Element.U8(renderScript))
        typeUcharY.setX(yRowStride).setY(height)
        val yAlloc = Allocation.createTyped(renderScript, typeUcharY.create())
        yAlloc.copyFrom(y)
        mYuv420._ypsIn = yAlloc

        val typeUcharUV = Type.Builder(renderScript, Element.U8(renderScript))
        // note that the size of the u's and v's are as follows:
        //      (  (width/2)*PixelStride + padding  ) * (height/2)
        // =    (RowStride                          ) * (height/2)
        // but I noted that on the S7 it is 1 less...
        typeUcharUV.setX(u.size)
        val uAlloc = Allocation.createTyped(renderScript, typeUcharUV.create())
        uAlloc.copyFrom(u)
        mYuv420._uIn = uAlloc

        val vAlloc = Allocation.createTyped(renderScript, typeUcharUV.create())
        vAlloc.copyFrom(v)
        mYuv420._vIn = vAlloc

        // handover parameters
        mYuv420._picWidth = width.toLong()
        mYuv420._uvRowStride = uvRowStride.toLong()
        mYuv420._uvPixelStride = uvPixelStride.toLong()

        val outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val outAlloc = Allocation.createFromBitmap(
            renderScript,
            outBitmap,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )

        val lo = Script.LaunchOptions()
        lo.setX(
            0,
            width
        )  // by this we ignore the y’s padding zone, i.e. the right side of x between width and yRowStride
        lo.setY(0, height)

        mYuv420.forEach_doConvert(outAlloc, lo)
        outAlloc.copyTo(outBitmap)

        return outBitmap
    }

}
