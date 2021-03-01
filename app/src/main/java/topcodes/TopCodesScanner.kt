package topcodes

import android.graphics.Bitmap
import kotlin.math.ceil

/**
 * Loads and scans images for TopCodes.  The algorithm does a single
 * sweep of an image (scanning one horizontal line at a time) looking
 * for a TopCode bullseye patterns.  If the pattern matches and the
 * black and white regions meet certain ratio constraints, then the
 * pixel is tested as the center of a candidate TopCode.
 *
 * @author Michael Horn
 * @version $Revision: 1.4 $, $Date: 2008/02/04 15:02:13 $
 */
open class TopCodesScanner {

    private lateinit var imageData: IntArray

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    var imageWidth = 0

    var imageHeight = 0

    private var maxWidthOfTopCodeInPixels = 80

    fun searchTopCodes(bitmap: Bitmap): List<TopCode> {

        imageWidth = bitmap.width
        imageHeight = bitmap.height

        imageData = IntArray(imageWidth * imageHeight)
        bitmap.getPixels(this.imageData, 0, imageWidth, 0, 0, imageWidth, imageHeight)

        threshold()
        return findCodes()
    }

    /**
     * Setting this to a reasonable value for
     * your application will reduce false positives (recognizing codes that
     * aren't actually there) and improve performance (because fewer
     * candidate codes will be tested).  Setting this value to as low as 50
     * or 60 pixels could be advisable for some applications.  However,
     * setting the maximum diameter too low will prevent valid codes from
     * being recognized. The default value is 640 pixels.
     */
    fun setMaxCodeDiameter(diameter: Int) {
        val f = diameter / 8.0f
        maxWidthOfTopCodeInPixels = ceil(f.toDouble()).toInt()
    }

    /**
     * Average of thresholded pixels in a 3x3 region around (x,y).
     * Returned value is between 0 (black) and 255 (white).
     */
    fun getSample3x3(x: Int, y: Int): Int {

        if (isMarginPixel(x, y)) return 0
        var pixel: Int
        var sum = 0
        for (j in y - 1..y + 1) {
            for (i in x - 1..x + 1) {
                pixel = imageData[j * imageWidth + i]
                if (pixel and 0x01000000 > 0) {
                    sum += 0xff
                }
            }
        }
        return sum / 9
    }

    /**
     * Average of thresholded pixels in a 3x3 region around (x,y).
     * Returned value is either 0 (black) or 1 (white).
     */
    fun getBW3x3(x: Int, y: Int): Int {
        if (isMarginPixel(x, y)) return 0
        var sum = 0
        for (j in y - 1..y + 1) {
            for (i in x - 1..x + 1) {
                val pixel = imageData[j * imageWidth + i]
                sum += pixel shr 24 and 0x01
            }
        }
        return if (sum >= 5) 1 else 0
    }

    private fun isMarginPixel(x: Int, y: Int) =
        isLeftOrRightMargin(x) || upOrDownMargin(y)

    private fun isLeftOrRightMargin(x: Int) = x < 1 || x > imageWidth - 2

    private fun upOrDownMargin(y: Int) = (y < 1 || y >= imageHeight - 2)

    /**
     * Perform Wellner adaptive thresholding to produce binary pixel
     * data. Also mark candidate spotcode locations.
     *
     * "Adaptive Thresholding for the DigitalDesk"
     * EuroPARC Technical Report EPC-93-110
     */
    private fun threshold() {
        var pixel: Int
        var red: Int
        var green: Int
        var blue: Int
        var alpha: Int
        var threshold: Int
        var sum = 128
        val s = 30
        var xy: Int
        var b1: Int
        var w1: Int
        var b2: Int
        var level: Int
        var dk: Int
        var candidateCount = 0
        for (y in 0 until imageHeight) {
            w1 = 0
            b2 = w1
            b1 = b2
            level = b1

            //----------------------------------------
            // Process rows back and forth (alternating
            // left-to-right, right-to-left)
            //----------------------------------------
            xy = if (y % 2 == 0) 0 else imageWidth - 1
            xy += y * imageWidth
            for (i in 0 until imageWidth) {

                //----------------------------------------
                // Calculate pixel intensity (0-255)
                //----------------------------------------
                pixel = imageData[xy]
                red = pixel shr 16 and 0xff
                green = pixel shr 8 and 0xff
                blue = pixel and 0xff
                alpha = (red + green + blue) / 3
                //a = r;

                //----------------------------------------
                // Calculate sum as an approximate sum
                // of the last s pixels
                //----------------------------------------
                sum += alpha - sum / s

                //----------------------------------------
                // Factor in sum from the previous row
                //----------------------------------------
                threshold = if (xy >= imageWidth) {
                    (sum + (imageData[xy - imageWidth] and 0xffffff)) / (2 * s)
                } else {
                    sum / s
                }

                //----------------------------------------
                // Compare the average sum to current pixel
                // to decide black or white
                //----------------------------------------
                val f = 0.975
                alpha = if (alpha < threshold * f) 0 else 1

                //----------------------------------------
                // Repack pixel data with binary data in
                // the alpha channel, and the running sum
                // for this pixel in the RGB channels
                //----------------------------------------
                imageData[xy] = (alpha shl 24) + (sum and 0xffffff)
                when (level) {
                    0 -> if (alpha == 0) {  // First black encountered
                        level = 1
                        b1 = 1
                        w1 = 0
                        b2 = 0
                    }
                    1 -> if (alpha == 0) {
                        b1++
                    } else {
                        level = 2
                        w1 = 1
                    }
                    2 -> if (alpha == 0) {
                        level = 3
                        b2 = 1
                    } else {
                        w1++
                    }
                    3 -> if (alpha == 0) {
                        b2++
                    } else {
                        var mask: Int
                        if (b1 >= 2 && b2 >= 2 && // less than 2 pixels... not interested
                            b1 <= maxWidthOfTopCodeInPixels && b2 <= maxWidthOfTopCodeInPixels && w1 <= maxWidthOfTopCodeInPixels + maxWidthOfTopCodeInPixels && Math.abs(
                                b1 + b2 - w1
                            ) <= b1 + b2 && Math.abs(
                                b1 + b2 - w1
                            ) <= w1 && Math.abs(b1 - b2) <= b1 && Math.abs(b1 - b2) <= b2
                        ) {
                            mask = 0x2000000
                            dk = 1 + b2 + w1 / 2
                            dk = if (y % 2 == 0) {
                                xy - dk
                            } else {
                                xy + dk
                            }
                            imageData[dk - 1] = imageData[dk - 1] or mask
                            imageData[dk] = imageData[dk] or mask
                            imageData[dk + 1] = imageData[dk + 1] or mask
                            candidateCount += 3 // count candidate codes
                        }
                        b1 = b2
                        w1 = 1
                        b2 = 0
                        level = 2
                    }
                }
                xy += if (y % 2 == 0) 1 else -1
            }
        }
    }

    private fun findCodes(): List<TopCode> {
        var testedCount = 0
        val spots = mutableListOf<TopCode>()
        var spot = TopCode()
        var k = imageWidth * 2
        for (j in 2 until imageHeight - 2) {
            for (i in 0 until imageWidth) {
                if (imageData[k] and 0x2000000 > 0) {
                    if (imageData[k - 1] and 0x2000000 > 0 &&
                        imageData[k + 1] and 0x2000000 > 0 &&
                        imageData[k - imageWidth] and 0x2000000 > 0 &&
                        imageData[k + imageWidth] and 0x2000000 > 0)
                    {
                        if (!overlaps(spots, i, j)) {
                            testedCount++
                            spot.decode(this, cx = i, cy = j)
                            if (spot.successfullyDecoded) {
                                spots.add(spot)
                                spot = TopCode()
                            }
                        }
                    }
                }
                k++
            }
        }
        return spots
    }

    private fun overlaps(topCodes: List<TopCode>, x: Int, y: Int) =
        topCodes.find { it.inBullsEye(x, y) } != null

    /**
     * Counts the number of vertical pixels from (x,y) until a color
     * change is perceived.
     */
    fun ydist(x: Int, y: Int, d: Int): Int {
        var sample: Int
        val start = getBW3x3(x, y)
        var j = y + d
        while (j > 1 && j < imageHeight - 1) {
            sample = getBW3x3(x, j)
            if (start + sample == 1) {
                return if (d > 0) j - y else y - j
            }
            j += d
        }
        return -1
    }

    /**
     * Counts the number of horizontal pixels from (x,y) until a color
     * change is perceived.
     */
    fun xdist(x: Int, y: Int, d: Int): Int {
        var sample: Int
        val start = getBW3x3(x, y)
        var i = x + d
        while (i > 1 && i < imageWidth - 1) {
            sample = getBW3x3(i, y)
            if (start + sample == 1) {
                return if (d > 0) i - x else x - i
            }
            i += d
        }
        return -1
    }

}