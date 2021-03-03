package topcodes

import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * TopCodes (Tangible Object Placement Codes) are black-and-white
 * circular fiducials designed to be recognized quickly by
 * low-resolution digital cameras with poor optics. The TopCode symbol
 * format is based on the open SpotCode format:
 *
 * http://www.highenergymagic.com/spotcode/symbols.html
 *
 * Each TopCode encodes a 13-bit number in a single data ring on the
 * outer edge of the symbol. Zero is represented by a black sector and
 * one is represented by a white sector.
 *
 * @author Michael Horn
 * @version $Revision: 1.4 $, $Date: 2007/10/15 13:12:30 $
 */
class TopCode {
    var code: Int

    /** The width of a single ring.  */
    var unit: Float

    var angleInRadians: Float

    var centerX: Float

    var centerY: Float

    /** Buffer used to decode sectors  */
    private var core: IntArray

    init {
        code = -1
        unit = 72.0f / WIDTH
        angleInRadians = 0f
        centerX = 0f
        centerY = 0f
        core = IntArray(WIDTH)
    }

    /**
     * Returns the diameter of this code in pixels.  This value
     * will be set automatically by the decode() function.
     */
    var diameter: Float
        get() = unit * WIDTH
        set(diameter) {
            unit = diameter / WIDTH
        }

    /**
     * Returns true if this code was sucessfully decoded.
     */
    val successfullyDecoded: Boolean
        get() = code > 0

    /**
     * Decodes a symbol given any point (cx, cy) inside the center
     * circle (bulls-eye) of the code.
     */
    fun decode(scanner: TopCodesScanner, cx: Int, cy: Int): Int {
        val up = scanner.ydist(cx, cy, -1) +
                scanner.ydist(cx - 1, cy, -1) +
                scanner.ydist(cx + 1, cy, -1)
        val down = scanner.ydist(cx, cy, 1) +
                scanner.ydist(cx - 1, cy, 1) +
                scanner.ydist(cx + 1, cy, 1)
        val left = scanner.xdist(cx, cy, -1) +
                scanner.xdist(cx, cy - 1, -1) +
                scanner.xdist(cx, cy + 1, -1)
        val right = scanner.xdist(cx, cy, 1) +
                scanner.xdist(cx, cy - 1, 1) +
                scanner.xdist(cx, cy + 1, 1)
        centerX = cx.toFloat()
        centerY = cy.toFloat()
        centerX += (right - left) / 6.0f
        centerY += (down - up) / 6.0f
        unit = readUnit(scanner)
        code = -1
        if (unit < 0) return -1
        var c: Int
        var maxc = 0
        var arca: Float
        var maxa = 0f
        var maxu = 0f

        //-----------------------------------------
        // Try different unit and arc adjustments,
        // save the one that produces a maximum
        // confidence reading...
        //-----------------------------------------
        for (u in -2..2) {
            for (a in 0..9) {
                arca = a * ARC * 0.1f
                c = readCode(
                    scanner,
                    unit + unit * 0.05f * u,
                    arca
                )
                if (c > maxc) {
                    maxc = c
                    maxa = arca
                    maxu = unit + unit * 0.05f * u
                }
            }
        }

        // One last call to readCode to reset orientation and code
        if (maxc > 0) {
            unit = maxu
            readCode(scanner, unit, maxa)
            code = rotateLowest(code, maxa)
        }
        return code
    }

    /**
     * Attempts to decode the binary pixels of an image into a code
     * value.
     *
     * scanner - image scanner
     * unit    - width of a single ring (codes are 8 units wide)
     * arca    - Arc adjustment.  Rotation correction delta value.
     */
    private fun readCode(scanner: TopCodesScanner, unit: Float, arca: Float): Int {
        var dx: Float
        var dy: Float // direction vector
        var dist: Float
        var c = 0
        var sx: Int
        var sy: Int
        var bit: Int
        var bits = 0
        code = -1
        for (sector in SECTORS - 1 downTo 0) {
            dx = cos(ARC * sector + arca.toDouble()).toFloat()
            dy = sin(ARC * sector + arca.toDouble()).toFloat()

            // Take 8 samples across the diameter of the symbol
            for (i in 0 until WIDTH) {
                dist = (i - 3.5f) * unit
                sx = (centerX + dx * dist).roundToInt()
                sy = (centerY + dy * dist).roundToInt()
                core[i] = scanner.getSample3x3(sx, sy)
            }

            // white rings
            if (core[1] <= 128 || core[3] <= 128 || core[4] <= 128 || core[6] <= 128
            ) {
                return 0
            }

            // black ring
            if (core[2] > 128 || core[5] > 128) {
                return 0
            }

            // compute confidence in core sample
            c += core[1] + core[3] + core[4] + core[6] +  // white rings
                    (0xff - core[2]) + (0xff - core[5]) // black ring

            // data rings
            c += Math.abs(core[7] * 2 - 0xff)

            // opposite data ring
            c += 0xff - Math.abs(core[0] * 2 - 0xff)
            bit = if (core[7] > 128) 1 else 0
            bits = bits shl 1
            bits += bit
        }
        return if (checksum(bits)) {
            code = bits
            c
        } else {
            0
        }
    }

    /**
     * rotateLowest() tries each of the possible rotations and returns
     * the lowest.
     */
    fun rotateLowest(bits: Int, arca: Float): Int {
        var bits = bits
        var arca = arca
        var min = bits
        val mask = 0x1fff

        // slightly overcorrect arc-adjustment
        // ideal correction would be (ARC / 2),
        // but there seems to be a positive bias
        // that falls out of the algorithm.
        arca -= ARC * 0.65f
        angleInRadians = 0f
        for (i in 1..SECTORS) {
            bits = bits shl 1 and mask or
                    (bits shr SECTORS - 1)
            if (bits < min) {
                min = bits
                angleInRadians = i * -ARC
            }
        }
        angleInRadians += arca
        return min
    }

    /**
     * Only codes with a checksum of 5 are valid
     */
    fun checksum(bits: Int): Boolean {
        var bits = bits
        var sum = 0
        for (i in 0 until SECTORS) {
            sum += bits and 0x01
            bits = bits shr 1
        }
        return sum == 5
    }

    /**
     * Returns true if the given point is inside the bulls-eye
     */
    fun inBullsEye(px: Int, py: Int): Boolean {
        return (centerX - px) * (centerX - px) + (centerY - py) * (centerY - py) <= unit * unit
    }

    /**
     * Determines the symbol's unit length by counting the number
     * of pixels between the outer edges of the first black ring.
     * North, south, east, and west readings are taken and the average
     * is returned.
     */
    private fun readUnit(scanner: TopCodesScanner): Float {
        val sx = centerX.roundToInt()
        val sy = centerY.roundToInt()
        val iwidth = scanner.imageWidth
        val iheight = scanner.imageHeight
        var whiteL = true
        var whiteR = true
        var whiteU = true
        var whiteD = true
        var sample: Int
        var distL = 0
        var distR = 0
        var distU = 0
        var distD = 0
        var i = 1
        while (true) {
            if (sx - i < 1 || sx + i >= iwidth - 1 || sy - i < 1 || sy + i >= iheight - 1 || i > 100
            ) {
                return (-1).toFloat()
            }

            // Left sample
            sample = scanner.getBW3x3(sx - i, sy)
            if (distL <= 0) {
                if (whiteL && sample == 0) {
                    whiteL = false
                } else if (!whiteL && sample == 1) {
                    distL = i
                }
            }

            // Right sample
            sample = scanner.getBW3x3(sx + i, sy)
            if (distR <= 0) {
                if (whiteR && sample == 0) {
                    whiteR = false
                } else if (!whiteR && sample == 1) {
                    distR = i
                }
            }

            // Up sample
            sample = scanner.getBW3x3(sx, sy - i)
            if (distU <= 0) {
                if (whiteU && sample == 0) {
                    whiteU = false
                } else if (!whiteU && sample == 1) {
                    distU = i
                }
            }

            // Down sample
            sample = scanner.getBW3x3(sx, sy + i)
            if (distD <= 0) {
                if (whiteD && sample == 0) {
                    whiteD = false
                } else if (!whiteD && sample == 1) {
                    distD = i
                }
            }
            if (distR > 0 && distL > 0 && distU > 0 && distD > 0) {
                val u = (distR + distL + distU + distD) / 8.0f
                return if (Math.abs(distR + distL - distU - distD) > u) {
                    (-1).toFloat()
                } else {
                    u
                }
            }
            i++
        }
    }

    companion object {
        var SECTORS = 13
        var WIDTH = 8
        var PI = Math.PI.toFloat()
        var ARC = 2 * PI / SECTORS

        fun generateCodes(): Array<TopCode?> {
            val n = 99
            var base = 0
            val list = arrayOfNulls<TopCode>(n)
            var code = TopCode()
            var bits: Int
            var count = 0
            while (count < n) {
                bits = code.rotateLowest(base, 0f)

                // Found a valid code
                if (bits == base && code.checksum(bits)) {
                    code.code = bits
                    code.angleInRadians = 0f
                    list[count++] = code
                    code = TopCode()
                }

                // Try next value
                base++
            }
            return list
        }
    }
}
