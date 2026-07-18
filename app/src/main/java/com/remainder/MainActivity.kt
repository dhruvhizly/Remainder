package com.remainder

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var preview: YearPreviewView
    private lateinit var stat: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preview = findViewById(R.id.preview)
        stat = findViewById(R.id.stat)

        preview.config = WallpaperConfig.load(this)
        setupColorPicker()
        setupModeToggle()
        setupSliders()
        updateStat()

        findViewById<MaterialButton>(R.id.setWallpaper).setOnClickListener {
            openWallpaperPicker()
        }
    }

    private fun setupColorPicker() {
        val picker = findViewById<ColorPickerView>(R.id.colorPicker)
        picker.setColor(preview.config.accent)
        updateColorReadout(preview.config.accent)
        picker.onColorChanged = { color ->
            preview.config = preview.config.copy(accent = color)
            WallpaperConfig.saveAccent(this, color)
            updateColorReadout(color)
        }
    }

    private fun updateColorReadout(color: Int) {
        findViewById<View>(R.id.colorPreview).background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
        findViewById<TextView>(R.id.colorHex).text =
            String.format("#%06X", 0xFFFFFF and color)
    }

    private fun setupModeToggle() {
        val toggle = findViewById<MaterialButtonToggleGroup>(R.id.modeToggle)
        toggle.check(R.id.modeLock)
        preview.mode = YearPreviewView.Mode.LOCK
        toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            preview.mode = when (checkedId) {
                R.id.modeHome -> YearPreviewView.Mode.HOME
                else -> YearPreviewView.Mode.LOCK
            }
        }
    }

    private fun setupSliders() {
        val cfg = preview.config
        val sizeSeek = findViewById<SeekBar>(R.id.sizeSeek)
        val posSeek = findViewById<SeekBar>(R.id.posSeek)

        sizeSeek.progress = toProgress(cfg.scale, WallpaperConfig.SCALE_MIN, WallpaperConfig.SCALE_MAX)
        posSeek.progress = toProgress(cfg.verticalBias, WallpaperConfig.BIAS_MIN, WallpaperConfig.BIAS_MAX)

        sizeSeek.setOnSeekBarChangeListener(object : SimpleSeekListener() {
            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                if (!fromUser) return
                val scale = fromProgress(p, WallpaperConfig.SCALE_MIN, WallpaperConfig.SCALE_MAX)
                preview.config = preview.config.copy(scale = scale)
                WallpaperConfig.saveScale(this@MainActivity, scale)
            }
        })

        posSeek.setOnSeekBarChangeListener(object : SimpleSeekListener() {
            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                if (!fromUser) return
                val bias = fromProgress(p, WallpaperConfig.BIAS_MIN, WallpaperConfig.BIAS_MAX)
                preview.config = preview.config.copy(verticalBias = bias)
                WallpaperConfig.saveVerticalBias(this@MainActivity, bias)
            }
        })
    }

    private fun toProgress(value: Float, min: Float, max: Float): Int =
        Math.round((value - min) / (max - min) * 100f).coerceIn(0, 100)

    private fun fromProgress(progress: Int, min: Float, max: Float): Float =
        min + (progress / 100f) * (max - min)

    /** SeekBar listener that only cares about progress changes. */
    private abstract class SimpleSeekListener : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(sb: SeekBar) {}
        override fun onStopTrackingTouch(sb: SeekBar) {}
    }

    override fun onResume() {
        super.onResume()
        // Reflect any accent chosen previously and advance the "today" dot.
        preview.config = WallpaperConfig.load(this)
        preview.invalidate()
        updateStat()
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
}
