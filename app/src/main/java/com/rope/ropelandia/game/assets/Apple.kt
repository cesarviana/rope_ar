package com.rope.ropelandia.game.assets

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.rope.ropelandia.R
import com.rope.ropelandia.game.Square
import kotlin.concurrent.thread

@SuppressLint("ViewConstructor")
class Apple(context: Context, square: Square, height: Int, width: Int) : Tile(square, height, width, context) {

    private val biteSound by lazy {
        MediaPlayer.create(context, R.raw.bite_sound)
    }
    private val apple by lazy {
        ResourcesCompat.getDrawable(context.resources, R.drawable.apple, null)!!
    }

    override fun reactToCollision() {
        if(this.isVisible) {
            this.visibility = INVISIBLE
            thread { biteSound.start() }.start()
        }
    }

    override fun getDrawable() = apple
}