package com.oldos.android.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oldos.android.R
import com.oldos.android.model.SettingsManager
import com.oldos.android.ui.components.*

@Composable
fun SettingsScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var subScreen by remember { mutableStateOf("home") }

    // Intercept back button to navigate backwards in Settings before leaving app
    BackHandler(enabled = subScreen != "home") {
        when (subScreen) {
            "about" -> subScreen = "general"
            else -> subScreen = "home"
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
                    onNavigateToWallpaper = { subScreen = "wallpaper" },
                    onNavigateToGeneral = { subScreen = "general" },
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
                    onNavigateToAbout = { subScreen = "about" },
                    onBack = { subScreen = "home" }
                )
            }
            "about" -> {
                AboutSettingsScreen(
                    onBack = { subScreen = "general" }
                )
            }
        }
    }
}

@Composable
private fun SettingsHomeScreen(
    onNavigateToWallpaper: () -> Unit,
    onNavigateToGeneral: () -> Unit,
    onHomeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFC5CCD4))
    ) {
        IOS4StatusBar()
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
                    onClick = {}
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Location Services",
                    iconRes = R.drawable.set_location,
                    detailText = "On",
                    showChevron = true,
                    onClick = {}
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Carrier",
                    iconRes = R.drawable.set_carrier,
                    detailText = SettingsManager.carrierName,
                    showChevron = true,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Group 2: Personalization & Sounds
            IOS4GroupCard {
                IOS4RowItem(
                    title = "Sounds",
                    iconRes = R.drawable.set_sound,
                    showChevron = true,
                    onClick = {}
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Brightness",
                    iconRes = R.drawable.set_brightness,
                    showChevron = true,
                    onClick = {}
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Wallpaper",
                    iconRes = R.drawable.set_wallpaper,
                    showChevron = true,
                    onClick = onNavigateToWallpaper
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Group 3: General System
            IOS4GroupCard {
                IOS4RowItem(
                    title = "General",
                    iconRes = R.drawable.set_general,
                    showChevron = true,
                    onClick = onNavigateToGeneral
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Mail, Contacts, Calendars",
                    iconRes = R.drawable.ic_mail,
                    showChevron = true,
                    onClick = {}
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Phone",
                    iconRes = R.drawable.ic_phone,
                    showChevron = true,
                    onClick = {}
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Safari",
                    iconRes = R.drawable.ic_safari,
                    showChevron = true,
                    onClick = {}
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Messages",
                    iconRes = R.drawable.ic_messages,
                    showChevron = true,
                    onClick = {}
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "iPod",
                    iconRes = R.drawable.ic_ipod,
                    showChevron = true,
                    onClick = {}
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Photos",
                    iconRes = R.drawable.ic_photos,
                    showChevron = true,
                    onClick = {}
                )
                IOS4RowDivider()
                IOS4RowItem(
                    title = "Notes",
                    iconRes = R.drawable.ic_notes,
                    showChevron = true,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
