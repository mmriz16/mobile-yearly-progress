package com.miftakhulrizky.yearprogress.wallpaper

import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.miftakhulrizky.yearprogress.model.YearProgressState
import java.time.LocalDate

class YearProgressWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = YearProgressEngine()

    private inner class YearProgressEngine : Engine() {
        private val renderer = YearProgressRenderer(this@YearProgressWallpaperService)
        private val handler = Handler(Looper.getMainLooper())
        private val drawRunnable = object : Runnable {
            override fun run() {
                drawFrame()
                handler.postDelayed(this, 1000L)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                drawRunnable.run()
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            drawFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunnable)
        }

        private fun drawFrame() {
            val holder = surfaceHolder ?: return
            val canvas = try {
                holder.lockCanvas()
            } catch (_: Exception) {
                null
            } ?: return

            try {
                val state = YearProgressState.from(LocalDate.now())
                renderer.draw(
                    canvas = canvas,
                    width = canvas.width,
                    height = canvas.height,
                    state = state,
                    pulseProgress = (System.currentTimeMillis() % 2400L) / 2400f,
                    accentColor = WallpaperPreferences.getAccentColor(this@YearProgressWallpaperService)
                )
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
