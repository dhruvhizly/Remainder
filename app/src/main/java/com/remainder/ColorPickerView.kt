package com.remainder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * A compact, dependency-free HSV colour picker:
 *   - a saturation/value square (drag to pick), and
 *   - a hue bar beneath it.
 *
 * Emits the selected colour through [onColorChanged] as the user drags.
 */
class ColorPickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var onColorChanged: ((Int) -> Unit)? = null

    private val hsv = floatArrayOf(28f, 0.9f, 1f) // default ~ orange
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = dp(2f)
    }
    private val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0x66000000
        strokeWidth = dp(4f)
    }

    private val svRect = RectF()
    private val hueRect = RectF()
    private val hueBarH = dp(26f)
    private val gap = dp(16f)
    private val radius = dp(12f)

    val color: Int get() = Color.HSVToColor(hsv)

    fun setColor(argb: Int) {
        Color.colorToHSV(argb, hsv)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val svH = h - hueBarH - gap
        svRect.set(0f, 0f, w.toFloat(), svH)
        hueRect.set(0f, svH + gap, w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        // --- Saturation/value square for the current hue ---
        val pureHue = Color.HSVToColor(floatArrayOf(hsv[0], 1f, 1f))
        fill.shader = LinearGradient(
            svRect.left, svRect.top, svRect.right, svRect.top,
            Color.WHITE, pureHue, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(svRect, radius, radius, fill)
        fill.shader = LinearGradient(
            svRect.left, svRect.top, svRect.left, svRect.bottom,
            Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(svRect, radius, radius, fill)
        fill.shader = null

        // SV selector
        val sx = svRect.left + hsv[1] * svRect.width()
        val sy = svRect.top + (1f - hsv[2]) * svRect.height()
        canvas.drawCircle(sx, sy, dp(9f), shadow)
        canvas.drawCircle(sx, sy, dp(9f), stroke)

        // --- Hue bar ---
        val hues = intArrayOf(
            Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
            Color.BLUE, Color.MAGENTA, Color.RED
        )
        fill.shader = LinearGradient(
            hueRect.left, hueRect.top, hueRect.right, hueRect.top,
            hues, null, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(hueRect, hueBarH / 2f, hueBarH / 2f, fill)
        fill.shader = null

        // Hue selector
        val hx = hueRect.left + (hsv[0] / 360f) * hueRect.width()
        val hy = hueRect.centerY()
        canvas.drawCircle(hx, hy, hueBarH / 2f + dp(1f), shadow)
        canvas.drawCircle(hx, hy, hueBarH / 2f + dp(1f), stroke)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.coerceIn(0f, width.toFloat())
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (y <= svRect.bottom + gap / 2f) {
                    hsv[1] = ((x - svRect.left) / svRect.width()).coerceIn(0f, 1f)
                    hsv[2] = (1f - (y - svRect.top) / svRect.height()).coerceIn(0f, 1f)
                } else {
                    hsv[0] = ((x - hueRect.left) / hueRect.width() * 360f).coerceIn(0f, 359.99f)
                }
                invalidate()
                onColorChanged?.invoke(color)
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
}
