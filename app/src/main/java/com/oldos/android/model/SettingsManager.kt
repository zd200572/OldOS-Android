package com.oldos.android.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.oldos.android.R

object SettingsManager {

    // Exposed state objects — SettingsStore observes these directly for
    // automatic DataStore persistence.
    @get:DrawableRes
    val currentWallpaperState = mutableIntStateOf(R.drawable.wallpaper_1)

    val isAirplaneModeState = mutableStateOf(false)
    val isWifiEnabledState = mutableStateOf(true)
    val isBluetoothEnabledState = mutableStateOf(true)
    val isLockSoundState = mutableStateOf(true)
    val isKeyboardClicksState = mutableStateOf(true)
    val isSilentModeState = mutableStateOf(false)
    val isVibrateOnRingState = mutableStateOf(true)
    val isVibrateOnSilentState = mutableStateOf(true)
    val brightnessLevelState = mutableFloatStateOf(0.75f)
    val isAutoBrightnessState = mutableStateOf(false)
    val isNotificationsEnabledState = mutableStateOf(true)
    val isLocationEnabledState = mutableStateOf(true)
    val is24HourTimeState = mutableStateOf(false)
    val carrierNameState = mutableStateOf("AT&T")

    var currentWallpaperRes: Int
        @DrawableRes get() = currentWallpaperState.intValue
        set(value) {
            currentWallpaperState.intValue = value
        }

    var isAirplaneMode: Boolean
        get() = isAirplaneModeState.value
        set(value) {
            isAirplaneModeState.value = value
        }

    var isWifiEnabled: Boolean
        get() = isWifiEnabledState.value
        set(value) {
            isWifiEnabledState.value = value
        }

    var isBluetoothEnabled: Boolean
        get() = isBluetoothEnabledState.value
        set(value) {
            isBluetoothEnabledState.value = value
        }

    var isLockSoundEnabled: Boolean
        get() = isLockSoundState.value
        set(value) {
            isLockSoundState.value = value
        }

    var isKeyboardClicks: Boolean
        get() = isKeyboardClicksState.value
        set(value) {
            isKeyboardClicksState.value = value
        }

    var isSilentMode: Boolean
        get() = isSilentModeState.value
        set(value) {
            isSilentModeState.value = value
        }

    var isVibrateOnRing: Boolean
        get() = isVibrateOnRingState.value
        set(value) {
            isVibrateOnRingState.value = value
        }

    var isVibrateOnSilent: Boolean
        get() = isVibrateOnSilentState.value
        set(value) {
            isVibrateOnSilentState.value = value
        }

    var brightnessLevel: Float
        get() = brightnessLevelState.floatValue
        set(value) {
            brightnessLevelState.floatValue = value
        }

    var isAutoBrightness: Boolean
        get() = isAutoBrightnessState.value
        set(value) {
            isAutoBrightnessState.value = value
        }

    var isNotificationsEnabled: Boolean
        get() = isNotificationsEnabledState.value
        set(value) {
            isNotificationsEnabledState.value = value
        }

    var isLocationEnabled: Boolean
        get() = isLocationEnabledState.value
        set(value) {
            isLocationEnabledState.value = value
        }

    var is24HourTime: Boolean
        get() = is24HourTimeState.value
        set(value) {
            is24HourTimeState.value = value
        }

    var carrierName: String
        get() = carrierNameState.value
        set(value) {
            carrierNameState.value = value
        }

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
