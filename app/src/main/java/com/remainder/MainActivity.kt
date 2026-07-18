package com.remainder

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var preview: YearPreviewView
    private lateinit var stat: TextView

    private val accents = listOf(
        "#FF7A18", // orange (default)
        "#26C6A6", // teal
        "#4C8DFF", // blue
        "#FF5C8A", // pink
        "#B7F03C"  // lime
    ).map { Color.parseColor(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preview = findViewById(R.id.preview)
        stat = findViewById(R.id.stat)

        preview.config = WallpaperConfig.load(this)
        buildSwatches()
        updateStat()

        findViewById<MaterialButton>(R.id.setWallpaper).setOnClickListener {
            openWallpaperPicker()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reflect any accent chosen previously and advance the "today" dot.
        preview.config = WallpaperConfig.load(this)
        preview.invalidate()
        updateStat()
    }

    private fun buildSwatches() {
        val container = findViewById<LinearLayout>(R.id.swatches)
        container.removeAllViews()
        val size = dp(44)
        val margin = dp(6)
        val selected = preview.config.accent

        accents.forEach { color ->
            val swatch = View(this)
            val lp = LinearLayout.LayoutParams(size, size)
            lp.marginEnd = margin
            swatch.layoutParams = lp
            swatch.background = ringDrawable(color, isSelected = color == selected)
            swatch.setOnClickListener {
                WallpaperConfig.saveAccent(this, color)
                preview.config = WallpaperConfig.load(this)
                buildSwatches()
            }
            container.addView(swatch)
        }
    }

    private fun ringDrawable(color: Int, isSelected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            if (isSelected) setStroke(dp(3), Color.WHITE)
        }
    }

    private fun updateStat() {
        val now = Calendar.getInstance()
        val dayOfYear = now.get(Calendar.DAY_OF_YEAR)
        val totalDays = now.getActualMaximum(Calendar.DAY_OF_YEAR)
        val daysLeft = totalDays - dayOfYear
        val percent = Math.round(dayOfYear * 100f / totalDays)
        stat.text = getString(R.string.stat_days_left, daysLeft) +
            "  ·  " + getString(R.string.stat_percent, percent, totalDays)
    }

    /**
     * Opens the system live-wallpaper preview for our service. Falls back to the
     * generic live-wallpaper chooser if the direct preview isn't available.
     */
    private fun openWallpaperPicker() {
        val component = ComponentName(this, YearWallpaperService::class.java)
        try {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
            }
            startActivity(intent)
        } catch (_: Exception) {
            try {
                startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
            } catch (_: Exception) {
                Toast.makeText(
                    this,
                    "Couldn't open the live wallpaper picker on this device.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
