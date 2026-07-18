package com.remainder

import android.content.Context
import android.graphics.Color

/**
 * User-tunable look of the wallpaper, persisted in SharedPreferences so the
 * live wallpaper service and the in-app preview always agree.
 *
 * [scale] and [verticalBias] let the user fit the grid around their device's
 * lock-screen clock/notifications, which vary a lot across OEMs:
 *   - [scale]        multiplies the dot size (1f = default).
 *   - [verticalBias] shifts the whole grid up/down as a fraction of screen
 *                    height (0f = default centre; positive moves it down).
 */
data class WallpaperConfig(
    val background: Int = Color.BLACK,
    val dimDot: Int = Color.parseColor("#3A3A3A"),
    val pastDot: Int = Color.parseColor("#EDEDED"),
    val accent: Int = Color.parseColor("#FF7A18"),
    val labelColor: Int = Color.parseColor("#8A8A8A"),
    val showFooter: Boolean = true,
    val scale: Float = 1f,
    val verticalBias: Float = 0f
) {
    companion object {
        private const val PREFS = "remainder_prefs"
        private const val KEY_ACCENT = "accent"
        private const val KEY_SCALE = "scale"
        private const val KEY_BIAS = "vertical_bias"

        // Bounds the sliders map onto — also used to clamp stored values.
        const val SCALE_MIN = 0.6f
        const val SCALE_MAX = 1.4f
        const val BIAS_MIN = -0.15f
        const val BIAS_MAX = 0.25f

        fun load(context: Context): WallpaperConfig {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            return WallpaperConfig(
                accent = prefs.getInt(KEY_ACCENT, Color.parseColor("#FF7A18")),
                scale = prefs.getFloat(KEY_SCALE, 1f).coerceIn(SCALE_MIN, SCALE_MAX),
                verticalBias = prefs.getFloat(KEY_BIAS, 0f).coerceIn(BIAS_MIN, BIAS_MAX)
            )
        }

        fun saveAccent(context: Context, accent: Int) = edit(context) { putInt(KEY_ACCENT, accent) }

        fun saveScale(context: Context, scale: Float) =
            edit(context) { putFloat(KEY_SCALE, scale.coerceIn(SCALE_MIN, SCALE_MAX)) }

        fun saveVerticalBias(context: Context, bias: Float) =
            edit(context) { putFloat(KEY_BIAS, bias.coerceIn(BIAS_MIN, BIAS_MAX)) }

        private inline fun edit(
            context: Context,
            block: android.content.SharedPreferences.Editor.() -> Unit
        ) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply(block).apply()
        }
    }
}
