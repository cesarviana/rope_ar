package com.rope.ropelandia.game.bitmaptaker

import android.content.Context
import android.os.Handler
import java.util.concurrent.ExecutorService

class BitmapTakerFactory {

    enum class Type {
        VIDEO, PHOTO
    }

    fun createBitmapTaker(
        context: Context,
        handler: Handler,
        executor: ExecutorService,
        type: Type,
        bitmapTookCallback: BitmapTaker.BitmapTookCallback
    ): BitmapTaker {
        return when (type) {
            Type.VIDEO -> VideoBitmapTaker(context, handler, executor, bitmapTookCallback)
            Type.PHOTO -> PhotoBitmapTaker(context, handler, executor, bitmapTookCallback)
        }
    }
}