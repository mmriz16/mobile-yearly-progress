package com.miftakhulrizky.yearprogress.wallpaper

import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.miftakhulrizky.yearprogress.model.HolidayManager
import com.miftakhulrizky.yearprogress.model.YearProgressState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

class YearProgressWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = YearProgressEngine()

    private inner class YearProgressEngine : Engine() {
        private val renderer = YearProgressRenderer(this@YearProgressWallpaperService)
        private val handler = Handler(Looper.getMainLooper())
        private val scope = CoroutineScope(Dispatchers.Main + Job())
        private var holidaysCache: Set<LocalDate> = emptySet()
        private var fetchJob: Job? = null

        private val drawRunnable = object : Runnable {
            override fun run() {
                drawFrame()
                handler.postDelayed(this, 1000L)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                drawRunnable.run()
                startFetchingHolidays()
            } else {
                handler.removeCallbacks(drawRunnable)
                stopFetchingHolidays()
            }
        }

        private fun startFetchingHolidays() {
            if (fetchJob?.isActive == true) return
            fetchJob = scope.launch {
                while (isActive) {
                    val year = LocalDate.now().year
                    holidaysCache = HolidayManager.getHolidays(this@YearProgressWallpaperService, year)
                    delay(60 * 60 * 1000L) // Refresh per hour while visible
                }
            }
        }

        private fun stopFetchingHolidays() {
            fetchJob?.cancel()
            fetchJob = null
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
            stopFetchingHolidays()
        }

        private fun drawFrame() {
            val holder = surfaceHolder ?: return
            val canvas = try {
                holder.lockCanvas()
            } catch (_: Exception) {
                null
            } ?: return

            try {
                val state = YearProgressState.from(LocalDate.now(), holidaysCache)
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
