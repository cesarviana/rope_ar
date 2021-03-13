package com.rope.ropelandia.game.blocksanalyser

import com.rope.ropelandia.model.Block

interface BlocksAnalyzer {
    fun analyze(blocks: List<Block>)
}

class BlocksAnalyzerComposite : BlocksAnalyzer {

    private val analyzers = mutableListOf<BlocksAnalyzer>()

    override fun analyze(blocks: List<Block>) {
        if(blocks.isNotEmpty()){
            analyzers.forEach {
                it.analyze(blocks)
            }
        }
    }

    fun addBlocksAnalyzer(analyser: BlocksAnalyzer): BlocksAnalyzerComposite {
        analyzers.add(analyser)
        return this
    }

}