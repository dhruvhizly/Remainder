package com.remainder

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import java.util.Calendar

/** In-app preview that reuses the exact same [YearRenderer] as the wallpaper. */
class YearPreviewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val renderer = YearRenderer()
    var config: WallpaperConfig = WallpaperConfig.load(context)
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        renderer.draw(canvas, width, height, config, Calendar.getInstance())
    }
}
