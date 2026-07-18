package com.remainder

import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.util.Calendar

/**
 * The live wallpaper. It draws the year view and redraws itself once a day
 * (at local midnight) so the "today" dot advances, plus whenever it becomes
 * visible again — which is when the user might have just changed the accent.
 */
class YearWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = YearEngine()

    inner class YearEngine : Engine() {

        private val renderer = YearRenderer()
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var width = 0
        private var height = 0

        private val midnightTick = Runnable {
            drawFrame()
            scheduleNextMidnight()
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible
            if (isVisible) {
                drawFrame()
                scheduleNextMidnight()
            } else {
                handler.removeCallbacks(midnightTick)
            }
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder, format: Int, width: Int, height: Int
        ) {
            this.width = width
            this.height = height
            drawFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            visible = false
            handler.removeCallbacks(midnightTick)
        }

        private fun scheduleNextMidnight() {
            handler.removeCallbacks(midnightTick)
            val now = Calendar.getInstance()
            val next = (now.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 1)
                set(Calendar.MILLISECOND, 0)
            }
            val delay = (next.timeInMillis - now.timeInMillis).coerceAtLeast(1000L)
            handler.postDelayed(midnightTick, delay)
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            val config = WallpaperConfig.load(this@YearWallpaperService)
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    renderer.draw(canvas, width, height, config, Calendar.getInstance())
                }
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (_: IllegalArgumentException) {
                        // Surface went away mid-draw; ignore.
                    }
                }
            }
        }
    }
}
