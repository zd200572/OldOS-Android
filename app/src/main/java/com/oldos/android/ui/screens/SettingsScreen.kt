package com.oldos.android.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oldos.android.R
import com.oldos.android.model.SettingsManager
import com.oldos.android.ui.components.*
import com.oldos.android.ui.components.ios4PinstripeBackground
import com.oldos.android.ui.components.rememberIOS4PinstripeBrush
import com.oldos.android.ui.components.IOS4StatusBarVariant

@Composable
fun SettingsScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var subScreen by remember { mutableStateOf("home") }
    var appSettingsTarget by remember { mutableStateOf<Pair<String, Int>?>(null) }

    val homeScreens = setOf("home")

    // Intercept back button to navigate backwards in Settings before leaving app
    BackHandler(enabled = subScreen !in homeScreens) {
        subScreen = when (subScreen) {
            "about" -> "general"
            "network", "bluetooth", "autolock", "passcode", "datetime", "keyboard" -> "general"
            else -> "home"
        }
    }

    AnimatedContent(
        targetState = subScreen,
        transitionSpec = {
            if (targetState != "home") {
                (slideInHorizontally { width -> width } + fadeIn())
                    .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut())
            } else {
                (slideInHorizontally { width -> -width / 3 } + fadeIn())
                    .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
            }
        },
        label = "settings_navigation"
    ) { currentSubScreen ->
        when (currentSubScreen) {
            "home" -> {
                SettingsHomeScreen(
                    onNavigate = { subScreen = it },
                    onOpenAppSettings = { target ->
                        appSettingsTarget = target
                        subScreen = "app"
                    },
                    onHomeClick = onHomeClick
                )
            }
            "wallpaper" -> {
                WallpaperSettingsScreen(
                    onBack = { subScreen = "home" }
                )
            }
            "general" -> {
                GeneralSettingsScreen(
                    onNavigate = { subScreen = it },
                    onBack = { subScreen = "home" }
                )
            }
            "about" -> {
                AboutSettingsScreen(
                    onBack = { subScreen = "general" }
                )
            }
            "sounds" -> SoundsSettingsScreen(onBack = { subScreen = "home" })
            "brightness" -> BrightnessSettingsScreen(onBack = { subScreen = "home" })
            "notifications" -> NotificationsSettingsScreen(onBack = { subScreen = "home" })
            "location" -> LocationSettingsScreen(onBack = { subScreen = "home" })
            "carrier" -> CarrierSettingsScreen(onBack = { subScreen = "home" })
            "network" -> NetworkSettingsScreen(onBack = { subScreen = "general" })
            "bluetooth" -> BluetoothSettingsScreen(onBack = { subScreen = "general" })
            "autolock" -> AutoLockSettingsScreen(onBack = { subScreen = "general" })
            "passcode" -> PasscodeSettingsScreen(onBack = { subScreen = "general" })
            "datetime" -> DateTimeSettingsScreen(onBack = { subScreen = "general" })
            "keyboard" -> KeyboardSettingsScreen(onBack = { subScreen = "general" })
            "app" -> {
                val target = appSettingsTarget
                if (target != null) {
                    AppSettingsScreen(
                        appName = target.first,
                        appIconRes = target.second,
                        onBack = { subScreen = "home" }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsHomeScreen(
    onNavigate: (String) -> Unit,
    onOpenAppSettings: (Pair<String, Int>) -> Unit,
    onHomeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .ios4PinstripeBackground(rememberIOS4PinstripeBrush())
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = "Settings",
            leftButtonText = "Home",
            onLeftButtonClick = onHomeClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            // Group 1: Connectivity
            IOS4GroupCard {
                IOS4RowItem(
                    title = "Airplane Mode",
                    iconRes = R.drawable.set_airplane,
                    trailingContent = {
                        IOS4Switch(
                            checked = SettingsManager.isAirplaneMode,
                            onCheckedChange = { SettingsManager.isAirplaneMode = it }
                        )
                    }
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Wi-Fi",
                    iconRes = R.drawable.set_wifi,
                    detailText = if (SettingsManager.isWifiEnabled) "Connected" else "Off",
                    showChevron = true,
                    onClick = {
                        SettingsManager.isWifiEnabled = !SettingsManager.isWifiEnabled
                    }
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Notifications",
                    iconRes = R.drawable.set_notifications,
                    showChevron = true,
                    onClick = { onNavigate("notifications") }
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Location Services",
                    iconRes = R.drawable.set_location,
                    detailText = if (SettingsManager.isLocationEnabled) "On" else "Off",
                    showChevron = true,
                    onClick = { onNavigate("location") }
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Carrier",
                    iconRes = R.drawable.set_carrier,
                    detailText = SettingsManager.carrierName,
                    showChevron = true,
                    onClick = { onNavigate("carrier") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Group 2: Personalization & Sounds
            IOS4GroupCard {
                IOS4RowItem(
                    title = "Sounds",
                    iconRes = R.drawable.set_sound,
                    showChevron = true,
                    onClick = { onNavigate("sounds") }
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Brightness",
                    iconRes = R.drawable.set_brightness,
                    showChevron = true,
                    onClick = { onNavigate("brightness") }
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Wallpaper",
                    iconRes = R.drawable.set_wallpaper,
                    showChevron = true,
                    onClick = { onNavigate("wallpaper") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Group 3: General System
            IOS4GroupCard {
                IOS4RowItem(
                    title = "General",
                    iconRes = R.drawable.set_general,
                    showChevron = true,
                    onClick = { onNavigate("general") }
                )
                IOS4RowDivider()
                AppSettingsRow("Mail, Contacts, Calendars", R.drawable.ic_mail, onOpenAppSettings)
                IOS4RowDivider()
                AppSettingsRow("Phone", R.drawable.ic_phone, onOpenAppSettings)
                IOS4RowDivider()
                AppSettingsRow("Safari", R.drawable.ic_safari, onOpenAppSettings)
                IOS4RowDivider()
                AppSettingsRow("Messages", R.drawable.ic_messages, onOpenAppSettings)
                IOS4RowDivider()
                AppSettingsRow("iPod", R.drawable.ic_ipod, onOpenAppSettings)
                IOS4RowDivider()
                AppSettingsRow("Photos", R.drawable.ic_photos, onOpenAppSettings)
                IOS4RowDivider()
                AppSettingsRow("Notes", R.drawable.ic_notes, onOpenAppSettings)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AppSettingsRow(
    title: String,
    iconRes: Int,
    onOpenAppSettings: (Pair<String, Int>) -> Unit
) {
    IOS4RowItem(
        title = title,
        iconRes = iconRes,
        showChevron = true,
        onClick = { onOpenAppSettings(title to iconRes) }
    )
}
