package com.miftakhulrizky.yearprogress.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    background = WallpaperBlack,
    onBackground = WhiteSmoke,
    surface = PanelBlack,
    onSurface = WhiteSmoke,
    primary = AccentRed
)

@Composable
fun YearProgressWallpaperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content
    )
}
