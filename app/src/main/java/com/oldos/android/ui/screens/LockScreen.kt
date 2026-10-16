package com.oldos.android.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.oldos.android.ui.components.*
import com.oldos.android.util.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val slideAnim = remember { Animatable(0f) }
    var isUnlocking by remember { mutableStateOf(false) }

    fun triggerUnlock() {
        if (isUnlocking) return
        isUnlocking = true

        // Play authentic iOS 4 click-clack unlock sound
        SoundManager.playUnlockSound()

        coroutineScope.launch {
            // Animate top header up and bottom slider down
            slideAnim.animateTo(
                targetValue = 140f,
                animationSpec = tween(durationMillis = 220)
            )
            delay(50)
            onUnlocked()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Classic Wallpaper
        IOS4Wallpaper()

        // 2. Bottom shadow vignette (as in iOS 4 lockscreen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // 3. Status Bar (Fixed at top)
        IOS4StatusBar(
            isLocked = true,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
        )

        // 4. Lock Header (Slides UP on unlock)
        val headerOffset = (-slideAnim.value).dp
        LockHeader(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp) // below status bar
                .offset(y = headerOffset)
                .zIndex(1f)
        )

        // 5. Slide to Unlock Footer (Slides DOWN on unlock)
        val footerOffset = slideAnim.value.dp
        SlideToUnlockBar(
            onUnlock = { triggerUnlock() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = footerOffset)
                .zIndex(1f)
        )
    }
}
