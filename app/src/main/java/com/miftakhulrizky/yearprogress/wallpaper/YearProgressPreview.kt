package com.miftakhulrizky.yearprogress.wallpaper

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import com.miftakhulrizky.yearprogress.model.YearProgressState

@Composable
fun YearProgressPreview(
    modifier: Modifier = Modifier,
    state: YearProgressState,
    pulse: Float,
    accentColor: Int
) {
    val context = LocalContext.current
    val renderer = remember(context) { YearProgressRenderer(context) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(412f / 915f)
    ) {
        renderer.draw(
            canvas = drawContext.canvas.nativeCanvas,
            width = size.width.toInt(),
            height = size.height.toInt(),
            state = state,
            pulseProgress = pulse,
            accentColor = accentColor
        )
    }
}
