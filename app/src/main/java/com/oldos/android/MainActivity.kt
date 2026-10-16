package com.oldos.android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.oldos.android.model.AppHistoryManager
import com.oldos.android.model.AppItem
import com.oldos.android.ui.components.IOS4HomeButton
import com.oldos.android.ui.components.MultitaskingBar
import com.oldos.android.ui.screens.*
import com.oldos.android.ui.theme.OldOSTheme
import com.oldos.android.util.SoundManager

enum class OSState {
    LOCKED,
    HOME,
    APP
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize audio effects
        SoundManager.init(this)

        // Safe edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Query real data once on startup to warm caches and log status
        Thread {
            val calls = com.oldos.android.data.SystemDataRepository.readCallLog(this@MainActivity, 5)
            val sms = com.oldos.android.data.SystemDataRepository.readSmsThreads(this@MainActivity, 50)
            android.util.Log.i("MainActivity", "Startup check -> Call log: ${calls.size}, SMS threads: ${sms.size}")
        }.start()

        setContent {
            OldOSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    var osState by remember { mutableStateOf(OSState.HOME) }
                    var currentOpenedApp by remember { mutableStateOf<AppItem?>(null) }
                    var isMultitaskingOpen by remember { mutableStateOf(false) }
                    var isSpringBoardEditMode by remember { mutableStateOf(false) }
                    var lastBackPressTime by remember { mutableLongStateOf(0L) }

                    // Android hardware/gesture back handler
                    BackHandler(enabled = true) {
                        val now = System.currentTimeMillis()
                        if (isMultitaskingOpen) {
                            isMultitaskingOpen = false
                            lastBackPressTime = 0L
                            return@BackHandler
                        }

                        // Double-press Back to toggle Multitasking
                        if (osState != OSState.LOCKED && (now - lastBackPressTime < 450L)) {
                            isMultitaskingOpen = true
                            lastBackPressTime = 0L
                            return@BackHandler
                        }
                        lastBackPressTime = now

                        // Single press behavior
                        when (osState) {
                            OSState.APP -> {
                                currentOpenedApp = null
                                osState = OSState.HOME
                            }
                            OSState.HOME -> {
                                if (isSpringBoardEditMode) {
                                    isSpringBoardEditMode = false
                                } else {
                                    osState = OSState.LOCKED
                                }
                            }
                            OSState.LOCKED -> {
                                finish()
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        // 1. Simulated iPhone screen viewport (occupies flexible screen height)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            // Multitasking Tray sits at bottom of screen viewport behind sliding content
                            if (osState != OSState.LOCKED) {
                                MultitaskingBar(
                                    onSelectApp = { selectedApp ->
                                        isMultitaskingOpen = false
                                        AppHistoryManager.onAppLaunched(selectedApp)
                                        currentOpenedApp = selectedApp
                                        osState = OSState.APP
                                    },
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )
                            }

                            // Screen content offset animation when multitasking opens
                            val contentOffsetY by animateDpAsState(
                                targetValue = if (isMultitaskingOpen) (-115).dp else 0.dp,
                                animationSpec = tween(durationMillis = 240),
                                label = "multitasking_slide"
                            )

                            // Main OS Viewport (slides up by 115dp to reveal multitasking tray)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(y = contentOffsetY)
                            ) {
                                AnimatedContent(
                                    targetState = osState,
                                    transitionSpec = {
                                        when {
                                            initialState == OSState.LOCKED && targetState == OSState.HOME -> {
                                                (fadeIn(tween(300)) + scaleIn(initialScale = 1.08f, animationSpec = tween(300)))
                                                    .togetherWith(fadeOut(tween(200)))
                                            }
                                            initialState == OSState.HOME && targetState == OSState.LOCKED -> {
                                                fadeIn(tween(250)).togetherWith(fadeOut(tween(200)))
                                            }
                                            targetState == OSState.APP -> {
                                                (fadeIn() + scaleIn(initialScale = 0.85f))
                                                    .togetherWith(fadeOut() + scaleOut(targetScale = 1.15f))
                                            }
                                            else -> {
                                                (fadeIn() + scaleIn(initialScale = 1.15f))
                                                    .togetherWith(fadeOut() + scaleOut(targetScale = 0.85f))
                                            }
                                        }
                                    },
                                    label = "os_screen_transition"
                                ) { state ->
                                    when (state) {
                                        OSState.LOCKED -> {
                                            LockScreen(
                                                onUnlocked = {
                                                    osState = OSState.HOME
                                                }
                                            )
                                        }
                                        OSState.HOME -> {
                                            SpringBoardScreen(
                                                onAppOpen = { selectedApp ->
                                                    AppHistoryManager.onAppLaunched(selectedApp)
                                                    currentOpenedApp = selectedApp
                                                    osState = OSState.APP
                                                },
                                                isEditMode = isSpringBoardEditMode,
                                                onEditModeChange = { isSpringBoardEditMode = it }
                                            )
                                        }
                                        OSState.APP -> {
                                            val app = currentOpenedApp
                                            if (app != null && app.id == "calculator") {
                                                CalculatorScreen(
                                                    onHomeClick = {
                                                        currentOpenedApp = null
                                                        osState = OSState.HOME
                                                    }
                                                )
                                            } else if (app != null && app.id == "settings") {
                                                SettingsScreen(
                                                    onHomeClick = {
                                                        currentOpenedApp = null
                                                        osState = OSState.HOME
                                                    }
                                                )
                                            } else if (app != null && app.id == "messages") {
                                                MessagesScreen(
                                                    onHomeClick = {
                                                        currentOpenedApp = null
                                                        osState = OSState.HOME
                                                    }
                                                )
                                            } else if (app != null && app.id == "phone") {
                                                PhoneScreen(
                                                    onHomeClick = {
                                                        currentOpenedApp = null
                                                        osState = OSState.HOME
                                                    }
                                                )
                                            } else if (app != null) {
                                                AppPlaceholderScreen(
                                                    app = app,
                                                    onHomeClick = {
                                                        currentOpenedApp = null
                                                        osState = OSState.HOME
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // If Multitasking is open, tapping the upper raised screen closes it
                                if (isMultitaskingOpen) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                isMultitaskingOpen = false
                                            }
                                    )
                                }
                            }

                            // Bottom upward-swipe gesture sensor to pull up Multitasking Bar
                            if (osState != OSState.LOCKED && !isMultitaskingOpen) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(26.dp)
                                        .align(Alignment.BottomCenter)
                                        .pointerInput(Unit) {
                                            detectDragGestures { _, dragAmount ->
                                                if (dragAmount.y < -15f) {
                                                    isMultitaskingOpen = true
                                                }
                                            }
                                        }
                                    )
                            }
                        }

                        // 2. Hardware Chin: Authentic iOS 4 Virtual Home Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(82.dp)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            IOS4HomeButton(
                                onSingleClick = {
                                    if (isMultitaskingOpen) {
                                        // Close multitasking
                                        isMultitaskingOpen = false
                                    } else if (osState == OSState.APP) {
                                        // Exit running app back to SpringBoard
                                        currentOpenedApp = null
                                        osState = OSState.HOME
                                    } else if (osState == OSState.HOME) {
                                        // Stop icon jiggle edit mode
                                        if (isSpringBoardEditMode) {
                                            isSpringBoardEditMode = false
                                        }
                                    }
                                },
                                onDoubleClick = {
                                    if (osState != OSState.LOCKED) {
                                        isMultitaskingOpen = !isMultitaskingOpen
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Apply immersive system UI hiding after setContent
        hideSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
    }

    private fun hideSystemUI() {
        try {
            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }
}
