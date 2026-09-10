package com.varnok.eslin.teeter.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.varnok.eslin.teeter.R
import com.varnok.eslin.teeter.domain.repository.SettingsRepository

enum class Sfx { TAP, SLIDE, PLACE, CRASH, WIN, LOSE, UNLOCK }

class SoundManager(context: Context, private val settings: SettingsRepository) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids = mapOf(
        Sfx.TAP to pool.load(context, R.raw.tap, 1),
        Sfx.SLIDE to pool.load(context, R.raw.slide, 1),
        Sfx.PLACE to pool.load(context, R.raw.place, 1),
        Sfx.CRASH to pool.load(context, R.raw.crash, 1),
        Sfx.WIN to pool.load(context, R.raw.win, 1),
        Sfx.LOSE to pool.load(context, R.raw.lose, 1),
        Sfx.UNLOCK to pool.load(context, R.raw.unlock, 1)
    )

    private var slideStream = 0

    fun play(sfx: Sfx, volume: Float = 1f, rate: Float = 1f) {
        if (!settings.sound()) return
        ids[sfx]?.let { pool.play(it, volume, volume, 1, 0, rate) }
    }

    fun startSlide() {
        if (!settings.sound() || slideStream != 0) return
        val id = ids[Sfx.SLIDE] ?: return
        slideStream = pool.play(id, 0.6f, 0.6f, 1, -1, 1f)
    }

    fun stopSlide() {
        if (slideStream != 0) {
            pool.stop(slideStream)
            slideStream = 0
        }
    }
}
