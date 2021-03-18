package com.rope.ropelandia

import com.rope.ropelandia.game.GameLoader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.URI
import java.util.*

class GameLoaderTest {

    @Test
    fun loadGameFromUrl() {
        val uuid = UUID.randomUUID()
        val urlString =
            "https://api.ctplatform.playerweb.com.br/test-applications/public/data/e9a5634e-4f98-4c65-b1b8-bee0a086b8f3/$uuid"
        val url = URI.create(urlString)
        GameLoader.load(url){ game ->
            assertThat(game.levels.size).isEqualTo(2)
        }
    }

}