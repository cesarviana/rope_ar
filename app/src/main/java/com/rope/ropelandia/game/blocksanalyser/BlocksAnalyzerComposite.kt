package com.rope.ropelandia.game.blocksanalyser

import android.util.Size
import com.rope.connection.RoPE
import com.rope.ropelandia.capture.BlocksToProgramConverter
import com.rope.ropelandia.capture.ProgramFactory
import com.rope.ropelandia.game.Game
import com.rope.ropelandia.game.numberOfLines
import com.rope.ropelandia.model.Block
import com.rope.ropelandia.model.RoPEBlock
import kotlin.math.abs

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

    fun addBlocksAnalyzer(analyser: BlocksAnalyzer) {
        analyzers.add(analyser)
    }

}