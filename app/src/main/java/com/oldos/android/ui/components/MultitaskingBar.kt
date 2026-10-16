package com.oldos.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.R
import com.oldos.android.model.AppHistoryManager
import com.oldos.android.model.AppItem

@Composable
fun MultitaskingBar(
    onSelectApp: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isKillMode by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isRotationLocked by remember { mutableStateOf(false) }

    // Page 0: Media & Rotation Lock controls, Page 1: Recent apps
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .background(Color(0xFF222222))
    ) {
        // 1. Classic iOS 4 Linen Texture Background
        Image(
            painter = painterResource(id = R.drawable.folder_switcher_bg),
            contentDescription = "Switcher Linen",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Inset Dark Drop Shadow (creates visual depth below the raised screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.40f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 3. Multitasking Pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) { page ->
            if (page == 0) {
                // Media Controls Page
                SwitcherMediaControls(
                    isPlaying = isPlaying,
                    onTogglePlay = { isPlaying = !isPlaying },
                    isRotationLocked = isRotationLocked,
                    onToggleRotationLock = { isRotationLocked = !isRotationLocked },
                    onIpodClick = {
                        val ipodApp = AppItem("ipod", "iPod", R.drawable.ic_ipod)
                        onSelectApp(ipodApp)
                    }
                )
            } else {
                // Recent Apps Page
                LazyRow(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(AppHistoryManager.recentApps, key = { it.id }) { app ->
                        SwitcherAppItem(
                            app = app,
                            isKillMode = isKillMode,
                            onClick = {
                                if (isKillMode) {
                                    isKillMode = false
                                } else {
                                    onSelectApp(app)
                                }
                            },
                            onLongClick = {
                                isKillMode = true
                            },
                            onKill = {
                                AppHistoryManager.killApp(app.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitcherAppItem(
    app: AppItem,
    isKillMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onKill: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "switcher_wiggle")
    val rotation by if (isKillMode) {
        infiniteTransition.animateFloat(
            initialValue = -2.2f,
            targetValue = 2.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 110, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wiggle_angle"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(66.dp)
            .rotate(rotation)
            .pointerInput(isKillMode) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
    ) {
        Box(contentAlignment = Alignment.TopStart) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(3.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = app.iconRes),
                    contentDescription = app.title,
                    modifier = Modifier.fillMaxSize()
                )

                // Classic top glossy shine
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.45f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Inner border
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                )
            }

            // Red Quit Box Badge (minus icon to kill app)
            if (isKillMode) {
                Image(
                    painter = painterResource(id = R.drawable.switcher_quit_box),
                    contentDescription = "Quit app",
                    modifier = Modifier
                        .offset(x = (-8).dp, y = (-8).dp)
                        .size(24.dp)
                        .clickable { onKill() }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Title
        Text(
            text = app.title,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SwitcherMediaControls(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    isRotationLocked: Boolean,
    onToggleRotationLock: () -> Unit,
    onIpodClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Orientation Lock Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onToggleRotationLock() }
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isRotationLocked) {
                                listOf(Color(0xFF2E62DE), Color(0xFF1B4AB5))
                            } else {
                                listOf(Color(0xFF555555), Color(0xFF333333))
                            }
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = if (isRotationLocked) R.drawable.rotation_lock else R.drawable.rotation_unlock
                    ),
                    contentDescription = "Rotation Lock",
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isRotationLocked) "Locked" else "Portrait",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Media Playback Controls (Prev, Play/Pause, Next)
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.mc_prev),
                contentDescription = "Previous",
                modifier = Modifier
                    .size(34.dp)
                    .clickable { /* Prev track */ }
            )

            Image(
                painter = painterResource(
                    id = if (isPlaying) R.drawable.mc_pause else R.drawable.mc_play
                ),
                contentDescription = "Play/Pause",
                modifier = Modifier
                    .size(42.dp)
                    .clickable { onTogglePlay() }
            )

            Image(
                painter = painterResource(id = R.drawable.mc_next),
                contentDescription = "Next",
                modifier = Modifier
                    .size(34.dp)
                    .clickable { /* Next track */ }
            )
        }

        // iPod App Icon & Label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onIpodClick() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_ipod),
                contentDescription = "iPod",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "iPod",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
