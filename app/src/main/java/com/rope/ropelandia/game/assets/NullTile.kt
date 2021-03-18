package com.rope.ropelandia.game.assets

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import com.rope.ropelandia.R
import com.rope.ropelandia.game.Square

class NullTile(context: Context) : Tile(Square(-1, -1), 0, 0, context) {
    private val empty = ResourcesCompat.getDrawable(context.resources, R.drawable.empty, null)!!
    override fun reactToCollision() {}
    override fun getDrawable() = empty
}
