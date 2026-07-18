package com.remainder

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import java.util.Calendar

/**
 * Draws the "Year View": twelve months laid out in a 3 x 4 grid. Each month
 * shows its name and, beneath it, a small grid of dots — one dot per day.
 *
 *   - Days already passed  -> bright ([WallpaperConfig.pastDot])
 *   - Today                -> accent color
 *   - Days still to come   -> dim ([WallpaperConfig.dimDot])
 *
 * The whole thing is measured in a single unit — the dot "pitch" (centre-to-
 * centre distance) — so it scales cleanly to any screen size or preview box.
 */
class YearRenderer {

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    private val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }

    private val monthNames = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    // --- Layout constants, expressed in dot-pitch units -----------------------
    private val monthCols = 3          // months across
    private val monthRows = 4          // months down
    private val dotCols = 7            // days across within a month
    private val dotRows = 5            // max weeks to reserve (ceil(31/7))
    private val monthGapU = 2.2f       // gap between month columns
    private val monthRowGapU = 2.2f    // gap between month rows
    private val labelU = 2.4f          // label band height above the dots

    private val unitsX = monthCols * dotCols + (monthCols - 1) * monthGapU
    private val monthCellU = labelU + dotRows
    private val unitsY = monthRows * monthCellU + (monthRows - 1) * monthRowGapU

    /**
     * @param canvas surface to draw on (already sized [width] x [height])
     * @param now    the moment to render; defaults are read from a Calendar
     */
    fun draw(canvas: Canvas, width: Int, height: Int, config: WallpaperConfig, now: Calendar) {
        canvas.drawColor(config.background)
        if (width <= 0 || height <= 0) return

        val year = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH)          // 0-based
        val currentDay = now.get(Calendar.DAY_OF_MONTH)     // 1-based
        val dayOfYear = now.get(Calendar.DAY_OF_YEAR)
        val totalDays = now.getActualMaximum(Calendar.DAY_OF_YEAR)

        // Reserve the top band for the lock-screen clock, keep a bottom margin.
        val bandTop = height * 0.30f
        val bandBottom = height * 0.90f
        val bandH = bandBottom - bandTop
        val padX = width * 0.09f
        val availW = width - 2 * padX

        // One pitch that satisfies both the width and the height budget, then
        // apply the user's size scale (clamped so it never overflows the width).
        val basePitch = minOf(availW / unitsX, bandH / (unitsY + 3.2f /* footer */))
        val maxPitch = (width * 0.98f) / unitsX
        val pitch = (basePitch * config.scale).coerceAtMost(maxPitch)

        val blockW = pitch * unitsX
        val blockH = pitch * unitsY
        val originX = (width - blockW) / 2f
        // Centre within the band, then shift by the user's vertical position bias.
        val originY = bandTop + (bandH - pitch * (unitsY + 3.2f)) / 2f + config.verticalBias * height

        val radius = pitch * 0.30f
        labelPaint.textSize = pitch * 1.55f

        val monthStepX = (dotCols + monthGapU) * pitch
        val monthStepY = (monthCellU + monthRowGapU) * pitch

        // Working calendar reused to find each month's length.
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(year, 0, 1)

        for (m in 0 until 12) {
            cal.set(Calendar.MONTH, m)
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            val col = m % monthCols
            val row = m / monthCols
            val monthX = originX + col * monthStepX
            val monthY = originY + row * monthStepY

            // Month name. The current month is drawn bright so it stands out.
            labelPaint.color = if (m == currentMonth) config.pastDot else config.labelColor
            labelPaint.typeface =
                if (m == currentMonth) Typeface.create("sans-serif-medium", Typeface.BOLD)
                else Typeface.create("sans-serif-medium", Typeface.NORMAL)
            canvas.drawText(monthNames[m], monthX, monthY + labelU * pitch * 0.62f, labelPaint)

            val dotsTop = monthY + labelU * pitch
            for (d in 1..daysInMonth) {
                val idx = d - 1
                val dc = idx % dotCols
                val dr = idx / dotCols
                val cx = monthX + (dc + 0.5f) * pitch
                val cy = dotsTop + (dr + 0.5f) * pitch

                dotPaint.color = when {
                    m < currentMonth || (m == currentMonth && d < currentDay) -> config.pastDot
                    m == currentMonth && d == currentDay -> config.accent
                    else -> config.dimDot
                }
                canvas.drawCircle(cx, cy, radius, dotPaint)
            }
        }

        // Footer: days remaining and progress percent.
        if (config.showFooter) {
            val daysLeft = totalDays - dayOfYear
            val percent = Math.round(dayOfYear * 100f / totalDays)
            footerPaint.color = config.accent
            footerPaint.textSize = pitch * 1.5f
            val footer = "${daysLeft}d left · ${percent}%"
            canvas.drawText(footer, width / 2f, originY + blockH + pitch * 2.4f, footerPaint)
        }
    }
}
