package com.oldos.android.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.R
import com.oldos.android.model.AppItem
import com.oldos.android.ui.components.*
import com.oldos.android.ui.theme.IOS4FontFamily

@Composable
fun SpringBoardScreen(
    onAppOpen: (AppItem) -> Unit,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    onEditModeChange: (Boolean) -> Unit = {},
    initialPage: Int = 1
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
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 3 })

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

        // Spotlight dims the whole screen (status bar + dock included)
        val spotlightScrimAlpha by animateFloatAsState(
            targetValue = if (pagerState.currentPage == 0 && !pagerState.isScrollInProgress) 0.59f
            else if (pagerState.currentPage == 0) 0.59f * ((1f - pagerState.currentPageOffsetFraction).coerceIn(0f, 1f))
            else 0f,
            label = "spotlight_scrim"
        )
        if (spotlightScrimAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = spotlightScrimAlpha))
            )
        }

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
                        SpotlightPage(
                            allApps = page1Apps + page2Apps + dockApps,
                            onAppOpen = onAppOpen
                        )
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
                        size = 60.dp,
                        showLabel = true,
                        showShadow = true,
                        isEditMode = isEditMode,
                        onClick = { onAppClick(app) },
                        onLongClick = onAppLongClick
                    )
                }
                if (row.size < 4) {
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.width(80.dp))
                    }
                }
            }
        }
    }
}

/**
 * iOS 4 Spotlight search page: dark translucent sheet over the wallpaper with
 * a rounded search field and result rows (icon + name), searching every
 * installed OldOS app.
 */
@Composable
private fun SpotlightPage(
    allApps: List<AppItem>,
    onAppOpen: (AppItem) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val results = remember(query, allApps) {
        if (query.isBlank()) {
            allApps
        } else {
            allApps.filter { it.title.contains(query.trim(), ignoreCase = true) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Search field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0x4D000000))
                    .border(0.5.dp, Color(0x66FFFFFF), RoundedCornerShape(9.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IOS4SpotlightFieldGlyph()
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 15.sp,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 15.sp,
                            fontFamily = IOS4FontFamily
                        ),
                        cursorBrush = SolidColor(Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Results: alternating translucent rows, iOS 4 spotlight style
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(results) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x14000000))
                            .clickable { onAppOpen(app) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = app.iconRes),
                            contentDescription = app.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = app.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = IOS4FontFamily,
                            modifier = Modifier.weight(1f)
                        )
                        IOS4ChevronGlyph(tint = Color.White.copy(alpha = 0.55f))
                    }
                }
            }
        }
    }
}

/** Small magnifier glyph inside the Spotlight search field. */
@Composable
private fun IOS4SpotlightFieldGlyph() {
    Canvas(modifier = Modifier.size(width = 13.dp, height = 13.dp)) {
        val stroke = 1.4.dp.toPx()
        val lensRadius = size.minDimension * 0.32f
        val lensCenter = Offset(size.width * 0.42f, size.height * 0.42f)

        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = lensRadius,
            center = lensCenter,
            style = Stroke(width = stroke)
        )
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(
                lensCenter.x + lensRadius * 0.72f,
                lensCenter.y + lensRadius * 0.72f
            ),
            end = Offset(size.width * 0.94f, size.height * 0.94f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}
