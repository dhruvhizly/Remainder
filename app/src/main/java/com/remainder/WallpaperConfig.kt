package com.remainder

import android.content.Context
import android.graphics.Color

/**
 * User-tunable look of the wallpaper, persisted in SharedPreferences so the
 * live wallpaper service and the in-app preview always agree.
 */
data class WallpaperConfig(
    val background: Int = Color.BLACK,
    val dimDot: Int = Color.parseColor("#3A3A3A"),
    val pastDot: Int = Color.parseColor("#EDEDED"),
    val accent: Int = Color.parseColor("#FF7A18"),
    val labelColor: Int = Color.parseColor("#8A8A8A"),
    val showFooter: Boolean = true
) {
    companion object {
        private const val PREFS = "remainder_prefs"
        private const val KEY_ACCENT = "accent"

        fun load(context: Context): WallpaperConfig {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val accent = prefs.getInt(KEY_ACCENT, Color.parseColor("#FF7A18"))
            return WallpaperConfig(accent = accent)
        }

        fun saveAccent(context: Context, accent: Int) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_ACCENT, accent)
                .apply()
        }
    }
}
