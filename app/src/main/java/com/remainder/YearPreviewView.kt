package com.remainder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * In-app preview that reuses the exact same [YearRenderer] as the wallpaper,
 * and overlays a mock of the system UI — the lock-screen clock or the home-
 * screen icons — so the user can size and position the grid to clear them.
 *
 * The overlay is drawn ONLY here in the preview; the real wallpaper never
 * paints system chrome (the OS does that).
 */
class YearPreviewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    enum class Mode { LOCK, HOME }

    private val renderer = YearRenderer()

    var config: WallpaperConfig = WallpaperConfig.load(context)
        set(value) {
            field = value
            invalidate()
        }

    var mode: Mode = Mode.LOCK
        set(value) {
            field = value
            invalidate()
        }

    private val chrome = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dateFmt = SimpleDateFormat("EEE d MMM", Locale.getDefault())
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        renderer.draw(canvas, width, height, config, Calendar.getInstance())
        when (mode) {
            Mode.LOCK -> drawLockChrome(canvas)
            Mode.HOME -> drawHomeChrome(canvas)
        }
    }

    /** Faux status bar + big clock, mirroring where the OS draws them. */
    private fun drawLockChrome(canvas: Canvas) {
        val w = width.toFloat()
        val now = Calendar.getInstance().time

        // Status-bar time, top-left.
        chrome.color = 0xCCFFFFFF.toInt()
        chrome.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        chrome.textSize = height * 0.020f
        chrome.textAlign = Paint.Align.LEFT
        canvas.drawText(timeFmt.format(now), w * 0.06f, height * 0.045f, chrome)

        // Battery pill, top-right.
        val pillW = w * 0.07f
        val pillH = height * 0.020f
        val pillR = RectF(w * 0.94f - pillW, height * 0.045f - pillH, w * 0.94f, height * 0.045f)
        chrome.color = 0x66FFFFFF.toInt()
        canvas.drawRoundRect(pillR, pillH / 2f, pillH / 2f, chrome)

        // Date line + large clock (the main keep-out zone).
        chrome.textAlign = Paint.Align.CENTER
        chrome.color = 0xDDFFFFFF.toInt()
        chrome.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        chrome.textSize = height * 0.024f
        canvas.drawText(dateFmt.format(now), w / 2f, height * 0.115f, chrome)

        chrome.color = 0xEEFFFFFF.toInt()
        chrome.typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)
        chrome.textSize = height * 0.14f
        canvas.drawText(timeFmt.format(now), w / 2f, height * 0.245f, chrome)
    }

    /** Faux home-screen icon rows + dock, showing the bottom keep-out zone. */
    private fun drawHomeChrome(canvas: Canvas) {
        val w = width.toFloat()
        val iconR = w * 0.05f
        val cols = 4
        val colStep = w * 0.22f
        val startX = (w - colStep * (cols - 1)) / 2f

        // A couple of icon rows near the bottom.
        chrome.color = 0x33FFFFFF.toInt()
        val rowYs = floatArrayOf(height * 0.72f, height * 0.82f)
        for (ry in rowYs) {
            for (c in 0 until cols) {
                canvas.drawRoundRect(
                    RectF(
                        startX + c * colStep - iconR, ry - iconR,
                        startX + c * colStep + iconR, ry + iconR
                    ),
                    iconR * 0.5f, iconR * 0.5f, chrome
                )
            }
        }

        // Dock pill along the bottom.
        val dockTop = height * 0.90f
        chrome.color = 0x22FFFFFF.toInt()
        canvas.drawRoundRect(
            RectF(w * 0.06f, dockTop, w * 0.94f, height * 0.965f),
            iconR, iconR, chrome
        )
        chrome.color = 0x44FFFFFF.toInt()
        val dockY = (dockTop + height * 0.965f) / 2f
        for (c in 0 until cols) {
            canvas.drawRoundRect(
                RectF(
                    startX + c * colStep - iconR, dockY - iconR,
                    startX + c * colStep + iconR, dockY + iconR
                ),
                iconR * 0.5f, iconR * 0.5f, chrome
            )
        }
    }
}
