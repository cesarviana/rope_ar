package com.rope.ropelandia

import com.rope.ropelandia.game.blocksanalyser.BlockSequenceFinder
import com.rope.ropelandia.model.ForwardBlock
import com.rope.ropelandia.model.LeftBlock
import com.rope.ropelandia.model.StartBlock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SequentialBlockSequenceFinderTest {

    @Test
    fun testHorizontalProgram() {
        val startBlock = StartBlock(10f, 70f, 20f, 0f)
        val forwardBlock = ForwardBlock(70f, 70f, 20f, 0f)

        val blocks = listOf(startBlock, forwardBlock)

        val program = BlockSequenceFinder.findSequence(blocks)

        assertThat(program)
            .hasSize(1)
            .containsSequence(forwardBlock)
    }

    @Test
    fun testHorizontalProgramWithDistantBlock() {
        val startBlock = StartBlock(10f, 100f, 20f, -90f)
        val forwardBlock = ForwardBlock(100f, 100f, 20f, 0f)
        val leftBlock = LeftBlock(1000f, 100f, 20f, 0f)

        val blocks = listOf(startBlock, forwardBlock, leftBlock)

        val program = BlockSequenceFinder.findSequence(blocks)

        assertThat(program)
            .hasSize(1)
            .doesNotContain(leftBlock)
            .containsSequence(forwardBlock)
    }

    @Test
    fun testHorizontalProgramInverted() {
        val startBlock = StartBlock(100f, 100f, 20f, 180f)
        val forwardBlock = ForwardBlock(10f, 100f, 20f, 0f)

        val blocks = listOf(startBlock, forwardBlock)

        val program = BlockSequenceFinder.findSequence(blocks)

        assertThat(program)
            .hasSize(1)
            .containsSequence(forwardBlock)
    }

    @Test
    fun testVerticalProgram() {
        val startBlock = StartBlock(100f, 10f, 20f, 180f)
        val forwardBlock = ForwardBlock(100f, 100f, 20f, 0f)

        val blocks = listOf(startBlock, forwardBlock)

        val program = BlockSequenceFinder.findSequence(blocks)

        assertThat(program)
            .hasSize(1)
            .containsSequence(forwardBlock)
    }

}