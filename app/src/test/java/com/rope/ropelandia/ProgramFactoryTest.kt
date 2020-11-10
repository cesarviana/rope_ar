package com.rope.ropelandia

import com.rope.ropelandia.capture.ProgramFactory
import com.rope.ropelandia.game.ForwardBlock
import com.rope.ropelandia.game.LeftBlock
import com.rope.ropelandia.game.StartBlock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ProgramFactoryTest {

    @Test
    fun testHorizontalProgram() {
        val startBlock = StartBlock(10f, 100f, 20f, 0f)
        val forwardBlock = ForwardBlock(100f, 100f, 20f, 0f)

        val blocks = listOf(startBlock, forwardBlock)

        val program = ProgramFactory.createProgram(blocks)

        assertThat(program)
            .hasSize(2)
            .containsSequence(startBlock, forwardBlock)
    }

    @Test
    fun testHorizontalProgramWithDistantBlock() {
        val startBlock = StartBlock(10f, 100f, 20f, 0f)
        val forwardBlock = ForwardBlock(100f, 100f, 20f, 0f)
        val leftBlock = LeftBlock(1000f, 100f, 20f, 0f)

        val blocks = listOf(startBlock, forwardBlock, leftBlock)

        val program = ProgramFactory.createProgram(blocks)

        assertThat(program)
            .hasSize(2)
            .doesNotContain(leftBlock)
            .containsSequence(startBlock, forwardBlock)
    }

    @Test
    fun testHorizontalProgramInverted() {
        val startBlock = StartBlock(100f, 100f, 20f, 180f)
        val forwardBlock = ForwardBlock(10f, 100f, 20f, 0f)

        val blocks = listOf(startBlock, forwardBlock)

        val program = ProgramFactory.createProgram(blocks)

        assertThat(program)
            .hasSize(2)
            .containsSequence(startBlock, forwardBlock)
    }

    @Test
    fun testVerticalProgram() {
        val startBlock = StartBlock(10f, 10f, 20f, 270f)
        val forwardBlock = ForwardBlock(100f, 100f, 20f, 0f)

        val blocks = listOf(startBlock, forwardBlock)

        val program = ProgramFactory.createProgram(blocks)

        assertThat(program)
            .hasSize(2)
            .containsSequence(startBlock, forwardBlock)
    }

}