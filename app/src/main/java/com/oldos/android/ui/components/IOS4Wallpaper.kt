package com.oldos.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.oldos.android.model.SettingsManager

@Composable
fun IOS4Wallpaper(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = SettingsManager.currentWallpaperRes),
        contentDescription = "iOS 4 Wallpaper",
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize()
    )
}
