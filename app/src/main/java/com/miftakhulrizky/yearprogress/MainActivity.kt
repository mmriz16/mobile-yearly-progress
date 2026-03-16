package com.miftakhulrizky.yearprogress

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miftakhulrizky.yearprogress.model.rememberCurrentYearProgressState
import com.miftakhulrizky.yearprogress.ui.theme.YearProgressWallpaperTheme
import com.miftakhulrizky.yearprogress.wallpaper.WallpaperPreferences
import com.miftakhulrizky.yearprogress.wallpaper.YearProgressPreview
import com.miftakhulrizky.yearprogress.wallpaper.YearProgressWallpaperService
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YearProgressWallpaperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    YearProgressApp()
                }
            }
        }
    }
}

@Composable
private fun YearProgressApp() {
    val context = LocalContext.current
    val state = rememberCurrentYearProgressState()
    var accentColor by remember {
        mutableIntStateOf(WallpaperPreferences.getAccentColor(context))
    }
    val scrollState = rememberScrollState()
    val pulse by produceState(initialValue = 0f) {
        while (true) {
            value = (System.currentTimeMillis() % 2400L) / 2400f
            delay(1000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111214))
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
            ) {
                YearProgressPreview(
                    modifier = Modifier.fillMaxWidth(),
                    state = state,
                    pulse = pulse,
                    accentColor = accentColor
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Wallpaper tahun berjalan yang mengikuti tanggal hari ini.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.76f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WallpaperPreferences.accentOptions.forEach { option ->
                    val isSelected = option == accentColor
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(option))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.24f),
                                shape = CircleShape
                            )
                            .clickable {
                                accentColor = option
                                WallpaperPreferences.setAccentColor(context, option)
                            }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val component = ComponentName(context, YearProgressWallpaperService::class.java)
                    val directIntent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                        putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
                    }
                    val fallbackIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        directIntent
                    } else {
                        fallbackIntent
                    }
                    context.startActivity(intent)
                }
            ) {
                Text("Set live wallpaper")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111214)
@Composable
private fun YearProgressAppPreview() {
    YearProgressWallpaperTheme {
        YearProgressApp()
    }
}
