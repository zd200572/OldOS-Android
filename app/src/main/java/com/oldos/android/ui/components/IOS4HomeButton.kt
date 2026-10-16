package com.oldos.android.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Authentic iOS 4 Virtual Hardware Home Button
 * Features:
 * - Circular concave physical button styling with rounded-rectangle icon
 * - Depression spring animation on press
 * - Authentic tactile haptic feedback
 * - Single click: Returns to Home, exits apps, stops SpringBoard jiggle mode
 * - Double click: Toggles Multitasking Bar (app switcher tray)
 */
@Composable
fun IOS4HomeButton(
    modifier: Modifier = Modifier,
    onSingleClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun triggerHaptic(isPressDown: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (isPressDown) {
                    VibrationEffect.createOneShot(28, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (isPressDown) 28L else 18L)
            }
        } catch (_: Exception) {}
    }

    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var pendingSingleClickJob by remember { mutableStateOf<Job?>(null) }
    var lastTapTimestamp by remember { mutableLongStateOf(0L) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "home_button_scale"
    )

    Box(
        modifier = modifier
            .size(72.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        triggerHaptic(isPressDown = true)
                        tryAwaitRelease()
                        isPressed = false
                        triggerHaptic(isPressDown = false)

                        val now = System.currentTimeMillis()
                        if (now - lastTapTimestamp < 320L) {
                            // Double tap confirmed! Cancel single tap and fire double tap immediately
                            pendingSingleClickJob?.cancel()
                            pendingSingleClickJob = null
                            lastTapTimestamp = 0L
                            onDoubleClick()
                        } else {
                            lastTapTimestamp = now
                            pendingSingleClickJob?.cancel()
                            pendingSingleClickJob = coroutineScope.launch {
                                delay(280L)
                                onSingleClick()
                                lastTapTimestamp = 0L
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer bezel ring
        Box(
            modifier = Modifier
                .size(66.dp)
                .background(Color(0xFF0A0A0A), CircleShape)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.18f),
                    shape = CircleShape
                )
        )

        // Inner concave button surface (scales on press)
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
                .shadow(
                    elevation = if (isPressed) 1.dp else 4.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1C1C1E),
                            Color(0xFF0F0F10),
                            Color(0xFF050505),
                            Color(0xFF262629).copy(alpha = 0.35f)
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.12f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Authentic iOS 4 Home icon: rounded-corner square
            Box(
                modifier = Modifier
                    .size(19.dp)
                    .border(
                        width = 1.8.dp,
                        color = Color(0xFF9E9EA4),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}
