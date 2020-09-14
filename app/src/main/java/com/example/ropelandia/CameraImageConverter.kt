package com.example.ropelandia

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.renderscript.*

// https://developer.android.com/guide/topics/renderscript/compute
// https://en.wikipedia.org/wiki/YUV

class CameraImageConverter() {

    private lateinit var renderScript: RenderScript
    private lateinit var scriptC: ScriptC_yuv4208888

    constructor(context: Context) : this() {
        this.renderScript = RenderScript.create(context)
        this.scriptC = ScriptC_yuv4208888(renderScript)
    }

    companion object ImagePlane {
        const val Y = 0
        const val U = 1
        const val V = 2
        const val UV = 1
    }

    fun convert(image: Image): Bitmap? {

        // Get the three image planes
        val planes = image.planes

        val yPlaneBytes = getImagePlaneBytes(planes, plane = Y)

        val uPlaneBytes = getImagePlaneBytes(planes, plane = U)

        val vPlaneBytes = getImagePlaneBytes(planes, plane = V)

        // get the relevant RowStrides and PixelStrides
        // (we know from documentation that PixelStride is 1 for y)
        val yRowStride = planes[Y].rowStride
        val uvRowStride =
            planes[UV].rowStride  // we know from   documentation that RowStride is the same for u and v.
        val uvPixelStride =
            planes[UV].pixelStride  // we know from   documentation that PixelStride is the same for u and v.

        // Y,U,V are defined as global allocations, the out-Allocation is the Bitmap.
        // Note also that uAlloc and vAlloc are 1-dimensional while yAlloc is 2-dimensional.

        val typeUcharY = Type.Builder(renderScript, Element.U8(renderScript))
        typeUcharY.setX(yRowStride).setY(image.height)

        val yPlaneBytesC = Allocation.createTyped(renderScript, typeUcharY.create())
        yPlaneBytesC.copyFrom(yPlaneBytes)

        val uvPlanesTypeC = Type.Builder(renderScript, Element.U8(renderScript))
        uvPlanesTypeC.setX(uPlaneBytes.size)

        val uPlaneBytesC = Allocation.createTyped(renderScript, uvPlanesTypeC.create())
        uPlaneBytesC.copyFrom(uPlaneBytes)

        val vPlaneBytesC = Allocation.createTyped(renderScript, uvPlanesTypeC.create())
        vPlaneBytesC.copyFrom(vPlaneBytes)


        val outputBitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)

        val outputAllocation = Allocation.createFromBitmap(
            renderScript,
            outputBitmap,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )


        scriptC._picWidth = image.width.toLong()
        scriptC._uvRowStride = uvRowStride.toLong()
        scriptC._uvPixelStride = uvPixelStride.toLong()

        scriptC._ypsIn = yPlaneBytesC
        scriptC._uIn = uPlaneBytesC
        scriptC._vIn = vPlaneBytesC


        val launchOptions = Script.LaunchOptions()
        launchOptions.setX(0, image.width)
        launchOptions.setY(0, image.height)

        scriptC.forEach_doConvert(outputAllocation)

        outputAllocation.copyTo(outputBitmap)

        return outputBitmap

    }

    private fun getImagePlaneBytes(
        imagePlanes: Array<Image.Plane>,
        plane: Int
    ): ByteArray {
        val planeBuffer = imagePlanes[plane].buffer
        val planeSize = planeBuffer.remaining()

        val bytes = ByteArray(planeSize)
        planeBuffer.get(bytes)

        return bytes
    }


}
