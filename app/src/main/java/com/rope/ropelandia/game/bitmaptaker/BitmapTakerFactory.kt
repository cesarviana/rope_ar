package com.rope.ropelandia.game.bitmaptaker

import android.content.Context
import android.os.Handler

class BitmapTakerFactory {

    enum class Type {
        STREAM, PICTURE
    }

    fun createBitmapTaker(context: Context, handler: Handler, type: Type, bitmapTookCallback: BitmapTookCallback) : BitmapTaker {
        return when(type) {
            Type.STREAM -> StreamBitmapTaker(context, handler, bitmapTookCallback)
            Type.PICTURE -> PhotoBitmapTaker(context, handler, bitmapTookCallback)
        }
    }
}