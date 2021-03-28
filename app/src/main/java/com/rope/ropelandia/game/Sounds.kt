package com.rope.ropelandia.game

import android.content.Context
import android.media.MediaPlayer
import com.rope.ropelandia.R

private const val BACKGROUND_VOLUME = 0.4f

object Sounds {

    private var lastSound: MediaPlayer? = null
    var gameEnd: MediaPlayer? = null
    var levelEnd: MediaPlayer? = null
    var connectionFailed: MediaPlayer? = null
    var backgroundHappy: MediaPlayer? = null
    var connectingSound: MediaPlayer? = null
    var connectedSound: MediaPlayer? = null

    fun initialize(context: Context) {
        val initialized = gameEnd != null
        if (initialized)
            return
        gameEnd = MediaPlayer.create(context, R.raw.game_end_sound).apply {
            setVolume(1f, 1f)
        }

        levelEnd = MediaPlayer.create(context, R.raw.level_end_sound).apply {
            setVolume(1f, 1f)
        }

        connectionFailed = MediaPlayer.create(
            context,
            R.raw.connection_fail_sound
        )

        connectingSound = MediaPlayer.create(context, R.raw.toy_connecting_sound)
        connectedSound = MediaPlayer.create(context, R.raw.toy_connected_sound)

        val even = (Math.random() * 10).toInt() % 2 == 0
        val id = if (even) R.raw.background_happy_sound_1 else R.raw.background_happy_sound_2
        backgroundHappy = MediaPlayer.create(
            context, id
        ).apply {
            setVolume(BACKGROUND_VOLUME, BACKGROUND_VOLUME)
        }
    }

    fun play(
        sound: MediaPlayer?,
        looping: Boolean = false,
        onCompletionListener: MediaPlayer.OnCompletionListener? = null
    ) {
        Thread {
            sound?.isLooping = looping
            sound?.start()
            onCompletionListener?.let {
                sound?.setOnCompletionListener(onCompletionListener)
            }
        }.start()
    }

    fun decreaseBackgroundVolume() {
        backgroundHappy?.setVolume(0.2f, 0.2f)
    }

    fun resetBackgroundVolume() {
        backgroundHappy?.setVolume(BACKGROUND_VOLUME, BACKGROUND_VOLUME)
    }

    fun stop(sound: MediaPlayer?) = sound?.stop()

}