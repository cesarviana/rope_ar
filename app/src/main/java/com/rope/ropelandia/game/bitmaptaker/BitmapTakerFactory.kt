package com.rope.ropelandia.game.bitmaptaker

import android.content.Context
import android.os.Handler
import java.util.concurrent.ExecutorService

class BitmapTakerFactory {

    enum class Type {
        STREAM, PICTURE
    }

    fun createBitmapTaker(
        context: Context,
        handler: Handler,
        executor: ExecutorService,
        type: Type,
        bitmapTookCallback: BitmapTaker.BitmapTookCallback
    ): BitmapTaker {
        return when (type) {
            Type.STREAM -> StreamBitmapTaker(context, handler, executor, bitmapTookCallback)
            Type.PICTURE -> PhotoBitmapTaker(context, handler, executor, bitmapTookCallback)
        }
    }
}