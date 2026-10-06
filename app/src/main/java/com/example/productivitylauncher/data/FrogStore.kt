package com.example.productivitylauncher.data

import android.content.Context

/**
 * Stores the single most important task ("frog") on the device.
 * Plain SharedPreferences is enough for one string. Everything stays local.
 */
class FrogStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("frog", Context.MODE_PRIVATE)

    fun get(): String = prefs.getString(KEY, "") ?: ""

    fun set(value: String) {
        prefs.edit().putString(KEY, value).apply()
    }

    private companion object {
        const val KEY = "frog_text"
    }
}
