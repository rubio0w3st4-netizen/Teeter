package com.varnok.eslin.teeter.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.varnok.eslin.teeter.domain.repository.SettingsRepository

class Haptics(context: Context, private val settings: SettingsRepository) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun buzz(ms: Long) {
        if (!settings.vibration() || !vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun tick() = buzz(14)
    fun nudge() = buzz(26)
    fun success() = buzz(60)
    fun error() = buzz(120)
}
