package com.oldos.android.model

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "oldos_settings")

/**
 * Persists SettingsManager through DataStore. Each tracked state object is
 * observed with snapshotFlow, so assignments from any screen (Carrier page,
 * switches, wallpaper picker…) are written automatically without manual
 * wiring. Call [init] once from Activity.onCreate.
 */
object SettingsStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var initialized = false

    private object Keys {
        val WALLPAPER = intPreferencesKey("wallpaper_res")
        val AIRPLANE = booleanPreferencesKey("airplane")
        val WIFI = booleanPreferencesKey("wifi")
        val BLUETOOTH = booleanPreferencesKey("bluetooth")
        val LOCK_SOUND = booleanPreferencesKey("lock_sound")
        val KEYBOARD_CLICKS = booleanPreferencesKey("keyboard_clicks")
        val SILENT = booleanPreferencesKey("silent")
        val VIBRATE_RING = booleanPreferencesKey("vibrate_ring")
        val VIBRATE_SILENT = booleanPreferencesKey("vibrate_silent")
        val BRIGHTNESS = floatPreferencesKey("brightness")
        val AUTO_BRIGHTNESS = booleanPreferencesKey("auto_brightness")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val LOCATION = booleanPreferencesKey("location")
        val TIME_24H = booleanPreferencesKey("time_24h")
        val CARRIER = stringPreferencesKey("carrier")
    }

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        val appContext = context.applicationContext

        // Restore persisted values into the states
        scope.launch {
            appContext.dataStore.data.collect { prefs ->
                SettingsManager.currentWallpaperState.intValue =
                    prefs[Keys.WALLPAPER] ?: SettingsManager.currentWallpaperState.intValue
                SettingsManager.isAirplaneModeState.value = prefs[Keys.AIRPLANE] ?: false
                SettingsManager.isWifiEnabledState.value = prefs[Keys.WIFI] ?: true
                SettingsManager.isBluetoothEnabledState.value = prefs[Keys.BLUETOOTH] ?: true
                SettingsManager.isLockSoundState.value = prefs[Keys.LOCK_SOUND] ?: true
                SettingsManager.isKeyboardClicksState.value = prefs[Keys.KEYBOARD_CLICKS] ?: true
                SettingsManager.isSilentModeState.value = prefs[Keys.SILENT] ?: false
                SettingsManager.isVibrateOnRingState.value = prefs[Keys.VIBRATE_RING] ?: true
                SettingsManager.isVibrateOnSilentState.value = prefs[Keys.VIBRATE_SILENT] ?: true
                SettingsManager.brightnessLevelState.floatValue = prefs[Keys.BRIGHTNESS] ?: 0.75f
                SettingsManager.isAutoBrightnessState.value = prefs[Keys.AUTO_BRIGHTNESS] ?: false
                SettingsManager.isNotificationsEnabledState.value = prefs[Keys.NOTIFICATIONS] ?: true
                SettingsManager.isLocationEnabledState.value = prefs[Keys.LOCATION] ?: true
                SettingsManager.is24HourTimeState.value = prefs[Keys.TIME_24H] ?: false
                SettingsManager.carrierNameState.value = prefs[Keys.CARRIER] ?: "AT&T"
            }
        }

        // Persist on every user-driven write
        fun <T> persist(state: MutableState<T>, key: androidx.datastore.preferences.core.Preferences.Key<T>) {
            scope.launch {
                snapshotFlow { state.value }.collect { value ->
                    appContext.dataStore.edit { prefs ->
                        prefs[key] = value
                    }
                }
            }
        }

        fun persistFloat(state: androidx.compose.runtime.MutableFloatState, key: androidx.datastore.preferences.core.Preferences.Key<Float>) {
            scope.launch {
                snapshotFlow { state.floatValue }.collect { value ->
                    appContext.dataStore.edit { prefs ->
                        prefs[key] = value
                    }
                }
            }
        }

        fun persistInt(state: androidx.compose.runtime.MutableIntState, key: androidx.datastore.preferences.core.Preferences.Key<Int>) {
            scope.launch {
                snapshotFlow { state.intValue }.collect { value ->
                    appContext.dataStore.edit { prefs ->
                        prefs[key] = value
                    }
                }
            }
        }

        persist(SettingsManager.isAirplaneModeState, Keys.AIRPLANE)
        persist(SettingsManager.isWifiEnabledState, Keys.WIFI)
        persist(SettingsManager.isBluetoothEnabledState, Keys.BLUETOOTH)
        persist(SettingsManager.isLockSoundState, Keys.LOCK_SOUND)
        persist(SettingsManager.isKeyboardClicksState, Keys.KEYBOARD_CLICKS)
        persist(SettingsManager.isSilentModeState, Keys.SILENT)
        persist(SettingsManager.isVibrateOnRingState, Keys.VIBRATE_RING)
        persist(SettingsManager.isVibrateOnSilentState, Keys.VIBRATE_SILENT)
        persist(SettingsManager.isAutoBrightnessState, Keys.AUTO_BRIGHTNESS)
        persist(SettingsManager.isNotificationsEnabledState, Keys.NOTIFICATIONS)
        persist(SettingsManager.isLocationEnabledState, Keys.LOCATION)
        persist(SettingsManager.is24HourTimeState, Keys.TIME_24H)
        persist(SettingsManager.carrierNameState, Keys.CARRIER)
        persistFloat(SettingsManager.brightnessLevelState, Keys.BRIGHTNESS)
        persistInt(SettingsManager.currentWallpaperState, Keys.WALLPAPER)
    }
}
