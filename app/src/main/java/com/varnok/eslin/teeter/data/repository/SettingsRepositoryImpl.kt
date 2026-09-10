package com.varnok.eslin.teeter.data.repository

import com.varnok.eslin.teeter.data.local.TeeterPrefs
import com.varnok.eslin.teeter.domain.repository.SettingsRepository

class SettingsRepositoryImpl(private val prefs: TeeterPrefs) : SettingsRepository {
    override fun sound(): Boolean = prefs.bool("sound", true)
    override fun setSound(value: Boolean) = prefs.putBool("sound", value)
    override fun vibration(): Boolean = prefs.bool("vibration", true)
    override fun setVibration(value: Boolean) = prefs.putBool("vibration", value)
    override fun backdrop(): Int = prefs.int("backdrop", 0)
    override fun setBackdrop(value: Int) = prefs.putInt("backdrop", value)
    override fun guides(): Boolean = prefs.bool("guides", true)
    override fun setGuides(value: Boolean) = prefs.putBool("guides", value)
}
