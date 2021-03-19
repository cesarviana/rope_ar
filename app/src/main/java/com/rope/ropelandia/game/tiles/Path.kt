package com.rope.ropelandia.game.tiles

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.res.ResourcesCompat
import com.rope.ropelandia.R
import com.rope.ropelandia.game.Square

@SuppressLint("ViewConstructor")
class Path(context: Context, square: Square, height: Int, width: Int) :
    Tile(square, height, width, context) {
    private val path = ResourcesCompat.getDrawable(context.resources, R.drawable.path, null)!!
    override fun reactToCollision() {}
    override fun getDrawable() = path
}