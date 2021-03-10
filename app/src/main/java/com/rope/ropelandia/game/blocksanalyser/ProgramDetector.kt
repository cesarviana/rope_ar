package com.rope.ropelandia.game.blocksanalyser

import com.rope.connection.RoPE
import com.rope.ropelandia.capture.BlocksToProgramConverter
import com.rope.ropelandia.capture.ProgramFactory
import com.rope.ropelandia.model.Block

abstract class ProgramDetector(val rope: RoPE) : BlocksAnalyzer {
    override fun analyze(blocks: List<Block>) {
        /**
         * If rope is executing, there is no need for search another programs.
         */
        if (rope.isExecuting())
            return
        val programBlocks = ProgramFactory.findSequence(blocks)
        if (programBlocks.isNotEmpty()) {
            val ropeProgram = BlocksToProgramConverter.convert(programBlocks)
            onFoundProgramBlocks(programBlocks, ropeProgram)
        }
    }

    abstract fun onFoundProgramBlocks(blocks: List<Block>, program: RoPE.Program)
}