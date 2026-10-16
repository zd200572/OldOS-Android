package com.oldos.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oldos.android.R
import com.oldos.android.model.AppItem
import com.oldos.android.ui.components.*

@Composable
fun SpringBoardScreen(
    onAppOpen: (AppItem) -> Unit,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    onEditModeChange: (Boolean) -> Unit = {}
) {
    // Page 1 apps: 4 columns * 6 rows = 24 classic iOS 4 apps
    val page1Apps = remember {
        listOf(
            // Row 1
            AppItem("messages", "Messages", R.drawable.ic_messages, badgeCount = 1),
            AppItem("calendar", "Calendar", R.drawable.ic_calendar),
            AppItem("photos", "Photos", R.drawable.ic_photos),
            AppItem("camera", "Camera", R.drawable.ic_camera),

            // Row 2
            AppItem("youtube", "YouTube", R.drawable.ic_youtube),
            AppItem("stocks", "Stocks", R.drawable.ic_stocks),
            AppItem("maps", "Maps", R.drawable.ic_maps),
            AppItem("weather", "Weather", R.drawable.ic_weather),

            // Row 3
            AppItem("voicememos", "Voice Memos", R.drawable.ic_voice_memos),
            AppItem("notes", "Notes", R.drawable.ic_notes),
            AppItem("clock", "Clock", R.drawable.ic_clock),
            AppItem("calculator", "Calculator", R.drawable.ic_calculator),

            // Row 4
            AppItem("settings", "Settings", R.drawable.ic_settings),
            AppItem("itunes", "iTunes", R.drawable.ic_itunes),
            AppItem("appstore", "App Store", R.drawable.ic_app_store),
            AppItem("gamecenter", "Game Center", R.drawable.ic_game_center),

            // Row 5
            AppItem("facetime", "FaceTime", R.drawable.ic_facetime),
            AppItem("videos", "Videos", R.drawable.ic_videos),
            AppItem("compass", "Compass", R.drawable.ic_compass),
            AppItem("contacts", "Contacts", R.drawable.ic_contacts),

            // Row 6
            AppItem("nike", "Nike+iPod", R.drawable.ic_nike),
            AppItem("music", "Music", R.drawable.ic_music),
            AppItem("cydia", "Cydia", R.drawable.ic_cydia),
            AppItem("folder_utilities", "Utilities", R.drawable.ic_folder, isFolder = true)
        )
    }

    // Page 2 apps
    val page2Apps = remember {
        listOf(
            AppItem("utilities_full", "Tools", R.drawable.ic_utilities)
        )
    }

    // Dock apps (4 pinned apps)
    val dockApps = remember {
        listOf(
            AppItem("phone", "Phone", R.drawable.ic_phone),
            AppItem("mail", "Mail", R.drawable.ic_mail, badgeCount = 2),
            AppItem("safari", "Safari", R.drawable.ic_safari),
            AppItem("ipod", "iPod", R.drawable.ic_ipod)
        )
    }

    // 0: Spotlight, 1: Page1, 2: Page2
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isEditMode) onEditModeChange(false)
            }
    ) {
        // 1. Classic Wallpaper
        IOS4Wallpaper()

        // 2. Main SpringBoard Content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Status Bar
            IOS4StatusBar()

            Spacer(modifier = Modifier.height(6.dp))

            // Pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> {
                        SpotlightPage()
                    }
                    1 -> {
                        AppIconGrid(
                            apps = page1Apps,
                            isEditMode = isEditMode,
                            onAppClick = { app ->
                                if (isEditMode) onEditModeChange(false)
                                else onAppOpen(app)
                            },
                            onAppLongClick = { onEditModeChange(true) }
                        )
                    }
                    2 -> {
                        AppIconGrid(
                            apps = page2Apps,
                            isEditMode = isEditMode,
                            onAppClick = { app ->
                                if (isEditMode) onEditModeChange(false)
                                else onAppOpen(app)
                            },
                            onAppLongClick = { onEditModeChange(true) }
                        )
                    }
                }
            }

            // Page Dots indicator
            IOS4PageIndicator(
                pageCount = 2,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Bottom 3D Glass Dock
            IOS4Dock(
                dockApps = dockApps,
                isEditMode = isEditMode,
                onAppClick = { app ->
                    if (isEditMode) onEditModeChange(false)
                    else onAppOpen(app)
                },
                onAppLongClick = { onEditModeChange(true) }
            )
        }
    }
}

@Composable
private fun AppIconGrid(
    apps: List<AppItem>,
    isEditMode: Boolean,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val rows = apps.chunked(4)
        for ((index, row) in rows.withIndex()) {
            if (index > 0) {
                // Tightly balanced 4x6 spacing
                Spacer(modifier = Modifier.height(13.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                for (app in row) {
                    IOS4AppIcon(
                        app = app,
                        size = 64.dp,
                        showLabel = true,
                        showShadow = true,
                        isEditMode = isEditMode,
                        onClick = { onAppClick(app) },
                        onLongClick = onAppLongClick
                    )
                }
                if (row.size < 4) {
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.width(82.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SpotlightPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Spotlight Search placeholder
    }
}
