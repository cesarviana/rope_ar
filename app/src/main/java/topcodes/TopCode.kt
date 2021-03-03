package topcodes

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
    var code: Int = -1
    var unit: Float
    var angleInRadians: Float
    var centerX: Float
    var centerY: Float

    init {
        unit = 72.0f / WIDTH
        angleInRadians = 0f
        centerX = 0f
        centerY = 0f
    }

    var diameter: Float
        get() = unit * WIDTH
        set(diameter) {
            unit = diameter / WIDTH
        }

    companion object {
        var WIDTH = 8
    }
}
