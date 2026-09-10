package com.varnok.eslin.teeter.data.local

import android.content.Context
import android.content.SharedPreferences

class TeeterPrefs(context: Context) {
    val store: SharedPreferences =
        context.getSharedPreferences("com.varnok.eslin.teeter_game_v1", Context.MODE_PRIVATE)

    fun int(key: String, def: Int) = store.getInt(key, def)
    fun putInt(key: String, value: Int) = store.edit().putInt(key, value).apply()
    fun bool(key: String, def: Boolean) = store.getBoolean(key, def)
    fun putBool(key: String, value: Boolean) = store.edit().putBoolean(key, value).apply()
    fun strings(key: String): Set<String> = store.getStringSet(key, emptySet()) ?: emptySet()
    fun putStrings(key: String, value: Set<String>) = store.edit().putStringSet(key, value).apply()
    fun wipe() = store.edit().clear().apply()
}
