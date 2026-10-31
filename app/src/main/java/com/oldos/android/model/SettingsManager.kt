package com.oldos.android.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.oldos.android.R

object SettingsManager {
    @get:DrawableRes
    var currentWallpaperRes by mutableIntStateOf(R.drawable.wallpaper_1)

    var isAirplaneMode by mutableStateOf(false)
    var isWifiEnabled by mutableStateOf(true)
    var isBluetoothEnabled by mutableStateOf(true)
    var isLockSoundEnabled by mutableStateOf(true)
    var carrierName by mutableStateOf("AT&T")

    // Settings sub-pages
    var isKeyboardClicks by mutableStateOf(true)
    var isSilentMode by mutableStateOf(false)
    var isVibrateOnRing by mutableStateOf(true)
    var isVibrateOnSilent by mutableStateOf(true)
    var brightnessLevel by mutableFloatStateOf(0.75f)
    var isAutoBrightness by mutableStateOf(false)
    var isNotificationsEnabled by mutableStateOf(true)
    var isLocationEnabled by mutableStateOf(true)
    var is24HourTime by mutableStateOf(false)
    var isAutoLock by mutableStateOf(true)

    val availableWallpapers = listOf(
        WallpaperItem(1, "Drops", R.drawable.wallpaper_1),
        WallpaperItem(2, "Lake", R.drawable.wallpaper_2),
        WallpaperItem(3, "Earth", R.drawable.wallpaper_3),
        WallpaperItem(4, "Grass", R.drawable.wallpaper_4),
        WallpaperItem(5, "Flower", R.drawable.wallpaper_5),
        WallpaperItem(6, "Cosmos", R.drawable.wallpaper_6),
        WallpaperItem(7, "Ocean", R.drawable.wallpaper_7),
        WallpaperItem(8, "Texture", R.drawable.wallpaper_8)
    )
}

data class WallpaperItem(
    val id: Int,
    val name: String,
    @DrawableRes val resId: Int
)
