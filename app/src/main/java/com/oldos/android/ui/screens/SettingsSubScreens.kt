package com.oldos.android.ui.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.oldos.android.R
import com.oldos.android.model.SettingsManager
import com.oldos.android.ui.components.IOS4GroupCard
import com.oldos.android.ui.components.IOS4RowDivider
import com.oldos.android.ui.components.IOS4RowItem
import com.oldos.android.ui.components.IOS4SettingsPage
import com.oldos.android.ui.components.IOS4Slider
import com.oldos.android.ui.components.IOS4Switch

/** Silence / vibrate / lock sounds. */
@Composable
fun SoundsSettingsScreen(onBack: () -> Unit) {
    IOS4SettingsPage(title = "Sounds", backButtonText = "Settings", onBack = onBack) {
        IOS4GroupCard {
            IOS4RowItem(
                title = "Silent",
                trailingContent = {
                    IOS4Switch(
                        checked = SettingsManager.isSilentMode,
                        onCheckedChange = { SettingsManager.isSilentMode = it }
                    )
                }
            )
            IOS4RowDivider()
            IOS4RowItem(
                title = "Vibrate",
                detailText = if (SettingsManager.isVibrateOnRing || SettingsManager.isVibrateOnSilent) "On" else "Off",
                showChevron = true,
                onClick = {}
            )
        }

        IOS4GroupCard(modifier = Modifier.padding(top = 10.dp)) {
            IOS4RowItem(
                title = "Vibrate on Ring",
                trailingContent = {
                    IOS4Switch(
                        checked = SettingsManager.isVibrateOnRing,
                        onCheckedChange = { SettingsManager.isVibrateOnRing = it }
                    )
                }
            )
            IOS4RowDivider()
            IOS4RowItem(
                title = "Vibrate on Silent",
                trailingContent = {
                    IOS4Switch(
                        checked = SettingsManager.isVibrateOnSilent,
                        onCheckedChange = { SettingsManager.isVibrateOnSilent = it }
                    )
                }
            )
        }

        IOS4GroupCard(
            headerTitle = "SOUNDS",
            modifier = Modifier.padding(top = 10.dp)
        ) {
            IOS4RowItem(
                title = "Lock Sound",
                trailingContent = {
                    IOS4Switch(
                        checked = SettingsManager.isLockSoundEnabled,
                        onCheckedChange = { SettingsManager.isLockSoundEnabled = it }
                    )
                }
            )
            IOS4RowDivider()
            IOS4RowItem(
                title = "Keyboard Clicks",
                trailingContent = {
                    IOS4Switch(
                        checked = SettingsManager.isKeyboardClicks,
                        onCheckedChange = { SettingsManager.isKeyboardClicks = it }
                    )
                }
            )
        }
    }
}

/** Brightness with a working slider that really changes the window brightness. */
@Composable
fun BrightnessSettingsScreen(onBack: () -> Unit) {
    val activity = LocalContext.current as? Activity

    IOS4SettingsPage(title = "Brightness", backButtonText = "Settings", onBack = onBack) {
        IOS4GroupCard {
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)) {
                IOS4Slider(
                    value = SettingsManager.brightnessLevel,
                    onValueChange = { value ->
                        SettingsManager.brightnessLevel = value
                        activity?.window?.let { window ->
                            if (!SettingsManager.isAutoBrightness) {
                                window.attributes = window.attributes.apply {
                                    screenBrightness = value.coerceIn(0.05f, 1f)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        IOS4GroupCard(modifier = Modifier.padding(top = 10.dp)) {
            IOS4RowItem(
                title = "Auto-Brightness",
                trailingContent = {
                    IOS4Switch(
                        checked = SettingsManager.isAutoBrightness,
                        onCheckedChange = { enabled ->
                            SettingsManager.isAutoBrightness = enabled
                            activity?.window?.let { window ->
                                window.attributes = window.attributes.apply {
                                    screenBrightness = if (enabled) -1f
                                    else SettingsManager.brightnessLevel.coerceIn(0.05f, 1f)
                                }
                            }
                        }
                    )
                }
            )
        }
    }
}

/** Notification toggles per system app (visual state). */
@Composable
fun NotificationsSettingsScreen(onBack: () -> Unit) {
    IOS4SettingsPage(title = "Notifications", backButtonText = "Settings", onBack = onBack) {
        IOS4GroupCard {
            IOS4RowItem(
                title = "Notifications",
                detailText = if (SettingsManager.isNotificationsEnabled) "On" else "Off",
                trailingContent = {
                    IOS4Switch(
                        checked = SettingsManager.isNotificationsEnabled,
                        onCheckedChange = { SettingsManager.isNotificationsEnabled = it }
                    )
                }
            )
        }

        if (SettingsManager.isNotificationsEnabled) {
            val appRows = listOf(
                "Messages" to R.drawable.ic_messages,
                "Phone" to R.drawable.ic_phone,
                "Mail" to R.drawable.ic_mail,
                "Calendar" to R.drawable.ic_calendar
            )
            IOS4GroupCard(modifier = Modifier.padding(top = 10.dp)) {
                appRows.forEachIndexed { index, (name, icon) ->
                    if (index > 0) IOS4RowDivider()
                    var enabled by remember(name) { mutableStateOf(true) }
                    IOS4RowItem(
                        title = name,
                        iconRes = icon,
                        trailingContent = {
                            IOS4Switch(checked = enabled, onCheckedChange = { enabled = it })
                        }
                    )
                }
            }
        }
    }
}

/** Location services master switch and per-app status. */
@Composable
fun LocationSettingsScreen(onBack: () -> Unit) {
    IOS4SettingsPage(title = "Location Services", backButtonText = "Settings", onBack = onBack) {
        IOS4GroupCard {
            IOS4RowItem(
                title = "Location Services",
                detailText = if (SettingsManager.isLocationEnabled) "On" else "Off",
                trailingContent = {
                    IOS4Switch(
                        checked = SettingsManager.isLocationEnabled,
                        onCheckedChange = { SettingsManager.isLocationEnabled = it }
                    )
                }
            )
        }

        if (SettingsManager.isLocationEnabled) {
            IOS4GroupCard(modifier = Modifier.padding(top = 10.dp)) {
                IOS4RowItem(title = "Maps", iconRes = R.drawable.ic_maps, detailText = "On")
                IOS4RowDivider()
                IOS4RowItem(title = "Camera", iconRes = R.drawable.ic_camera, detailText = "On")
                IOS4RowDivider()
                IOS4RowItem(title = "Weather", iconRes = R.drawable.ic_weather, detailText = "On")
            }
        }
    }
}

/** Carrier selection — updates the status bar carrier name. */
@Composable
fun CarrierSettingsScreen(onBack: () -> Unit) {
    val carriers = listOf("AT&T", "中国移动", "中国联通", "中国电信", "Vodafone", "NTT docomo")

    IOS4SettingsPage(title = "Carrier", backButtonText = "Settings", onBack = onBack) {
        IOS4GroupCard {
            IOS4RowItem(
                title = "Automatic",
                trailingContent = {
                    IOS4Switch(checked = true, onCheckedChange = {})
                }
            )
        }

        IOS4GroupCard(modifier = Modifier.padding(top = 10.dp)) {
            carriers.forEachIndexed { index, carrier ->
                if (index > 0) IOS4RowDivider(hasIcon = false)
                IOS4RowItem(
                    title = carrier,
                    detailText = if (SettingsManager.carrierName == carrier) "✓" else null,
                    onClick = { SettingsManager.carrierName = carrier }
                )
            }
        }
    }
}

/** Generic per-app settings page used by the app rows in Settings home. */
@Composable
fun AppSettingsScreen(
    appName: String,
    appIconRes: Int,
    onBack: () -> Unit
) {
    var showNotifications by remember { mutableStateOf(true) }
    var showBadges by remember { mutableStateOf(true) }

    IOS4SettingsPage(title = appName, backButtonText = "Settings", onBack = onBack) {
        IOS4GroupCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = appIconRes),
                    contentDescription = appName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }

        IOS4GroupCard(modifier = Modifier.padding(top = 10.dp)) {
            IOS4RowItem(
                title = "Notifications",
                trailingContent = {
                    IOS4Switch(checked = showNotifications, onCheckedChange = { showNotifications = it })
                }
            )
            IOS4RowDivider()
            IOS4RowItem(
                title = "Badge App Icon",
                trailingContent = {
                    IOS4Switch(checked = showBadges, onCheckedChange = { showBadges = it })
                }
            )
        }

        IOS4GroupCard(
            footerText = "$appName settings are recreated faithfully from iOS 4.",
            modifier = Modifier.padding(top = 10.dp)
        ) {}
    }
}

// ── General sub-pages ────────────────────────────────────────────────────────

@Composable
fun NetworkSettingsScreen(onBack: () -> Unit) {
    IOS4SettingsPage(title = "Network", backButtonText = "General", onBack = onBack) {
        IOS4GroupCard {
            IOS4RowItem(
                title = "Wi-Fi",
                detailText = if (SettingsManager.isWifiEnabled) "Connected" else "Off",
                showChevron = true,
                onClick = { SettingsManager.isWifiEnabled = !SettingsManager.isWifiEnabled }
            )
            IOS4RowDivider()
            IOS4RowItem(
                title = "Data Roaming",
                detailText = if (SettingsManager.isAirplaneMode) "Off" else "On"
            )
            IOS4RowDivider()
            IOS4RowItem(title = "VPN", detailText = "Not Connected")
        }
    }
}

@Composable
fun BluetoothSettingsScreen(onBack: () -> Unit) {
    IOS4SettingsPage(title = "Bluetooth", backButtonText = "General", onBack = onBack) {
        IOS4GroupCard {
            IOS4RowItem(
                title = "Bluetooth",
                detailText = if (SettingsManager.isBluetoothEnabled) "On" else "Off",
                trailingContent = {
                    IOS4Switch(
                        checked = SettingsManager.isBluetoothEnabled,
                        onCheckedChange = { SettingsManager.isBluetoothEnabled = it }
                    )
                }
            )
        }
    }
}

@Composable
fun AutoLockSettingsScreen(onBack: () -> Unit) {
    val options = listOf("1 Minute", "2 Minutes", "3 Minutes", "5 Minutes", "Never")
    var selected by remember { mutableStateOf("1 Minute") }

    IOS4SettingsPage(title = "Auto-Lock", backButtonText = "General", onBack = onBack) {
        IOS4GroupCard {
            options.forEachIndexed { index, option ->
                if (index > 0) IOS4RowDivider(hasIcon = false)
                IOS4RowItem(
                    title = option,
                    detailText = if (selected == option) "✓" else null,
                    onClick = { selected = option }
                )
            }
        }
    }
}

@Composable
fun PasscodeSettingsScreen(onBack: () -> Unit) {
    var isPasscodeOn by remember { mutableStateOf(false) }

    IOS4SettingsPage(title = "Passcode Lock", backButtonText = "General", onBack = onBack) {
        IOS4GroupCard {
            IOS4RowItem(
                title = "Passcode Lock",
                trailingContent = {
                    IOS4Switch(checked = isPasscodeOn, onCheckedChange = { isPasscodeOn = it })
                }
            )
        }
        if (isPasscodeOn) {
            IOS4GroupCard(modifier = Modifier.padding(top = 10.dp)) {
                IOS4RowItem(title = "Require Passcode", detailText = "Immediately", showChevron = true, onClick = {})
                IOS4RowDivider()
                IOS4RowItem(title = "Simple Passcode", detailText = "On")
            }
        }
    }
}

@Composable
fun DateTimeSettingsScreen(onBack: () -> Unit) {
    IOS4SettingsPage(title = "Date & Time", backButtonText = "General", onBack = onBack) {
        IOS4GroupCard {
            IOS4RowItem(
                title = "24-Hour Time",
                trailingContent = {
                    IOS4Switch(
                        checked = SettingsManager.is24HourTime,
                        onCheckedChange = { SettingsManager.is24HourTime = it }
                    )
                }
            )
        }
        IOS4GroupCard(modifier = Modifier.padding(top = 10.dp)) {
            IOS4RowItem(title = "Set Automatically", detailText = "On")
            IOS4RowDivider()
            IOS4RowItem(title = "Time Zone", detailText = "Shanghai")
        }
    }
}

@Composable
fun KeyboardSettingsScreen(onBack: () -> Unit) {
    var autoCorrect by remember { mutableStateOf(true) }
    var checkSpelling by remember { mutableStateOf(true) }
    var capitalize by remember { mutableStateOf(true) }

    IOS4SettingsPage(title = "Keyboard", backButtonText = "General", onBack = onBack) {
        IOS4GroupCard {
            IOS4RowItem(
                title = "Auto-Correction",
                trailingContent = { IOS4Switch(checked = autoCorrect, onCheckedChange = { autoCorrect = it }) }
            )
            IOS4RowDivider()
            IOS4RowItem(
                title = "Check Spelling",
                trailingContent = { IOS4Switch(checked = checkSpelling, onCheckedChange = { checkSpelling = it }) }
            )
            IOS4RowDivider()
            IOS4RowItem(
                title = "Auto-Capitalization",
                trailingContent = { IOS4Switch(checked = capitalize, onCheckedChange = { capitalize = it }) }
            )
        }
    }
}
