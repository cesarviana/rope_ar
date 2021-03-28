package com.rope.ropelandia.game.blocksanalyser

import com.rope.ropelandia.model.Block

abstract class SnailDetector : BlocksAnalyzer {
    override fun analyze(blocks: List<Block>) {

    }

    abstract fun snailArrivedNearBlocks()
}