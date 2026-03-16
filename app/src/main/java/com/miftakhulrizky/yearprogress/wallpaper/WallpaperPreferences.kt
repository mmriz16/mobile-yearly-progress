package com.miftakhulrizky.yearprogress.wallpaper

import android.content.Context

object WallpaperPreferences {
    private const val PREFS_NAME = "year_progress_wallpaper"
    private const val KEY_ACCENT = "accent_color"
    const val DEFAULT_ACCENT = 0xFFFD2639.toInt()

    val accentOptions = listOf(
        0xFFFD2639.toInt(),
        0xFFFF7A00.toInt(),
        0xFF35C759.toInt(),
        0xFF00C2FF.toInt(),
        0xFFFFD60A.toInt()
    )

    fun getAccentColor(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_ACCENT, DEFAULT_ACCENT)

    fun setAccentColor(context: Context, color: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_ACCENT, color)
            .apply()
    }
}
