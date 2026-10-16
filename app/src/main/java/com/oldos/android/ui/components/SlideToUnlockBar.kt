package com.oldos.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun SlideToUnlockBar(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "slide to unlock"
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    val thumbWidth = 72.dp
    val thumbWidthPx = with(density) { thumbWidth.toPx() }

    val dragOffset = remember { Animatable(0f) }
    val maxDragPx = (trackWidthPx - thumbWidthPx).coerceAtLeast(0f)

    // Infinite sweeping shimmer gradient for "slide to unlock"
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_transition")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_phase"
    )

    // Footer container with iOS 4 dark gradient
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x99292828),
                        Color(0xB3151414),
                        Color(0xD9000000)
                    )
                )
            )
            .border(
                width = 0.75.dp,
                color = Color.Black
            )
            .padding(horizontal = 24.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner Dark Slider Track (the groove)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { trackWidthPx = it.width.toFloat() }
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF030303),
                            Color(0xFF151515),
                            Color(0xFF222222)
                        )
                    )
                )
                .border(
                    width = 0.75.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF505050),
                            Color(0xFF8F8F8F)
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Text opacity fades as thumb advances
            val textAlpha = if (maxDragPx > 0f) {
                (1f - (dragOffset.value / maxDragPx) * 1.6f).coerceIn(0f, 1f)
            } else 1f

            // Shimmer text
            val shimmerBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF505050),
                    Color(0xFF858585),
                    Color.White,
                    Color(0xFF858585),
                    Color(0xFF505050)
                ),
                start = Offset(trackWidthPx * (shimmerPhase - 0.35f), 0f),
                end = Offset(trackWidthPx * (shimmerPhase + 0.35f), 0f)
            )

            Text(
                text = text,
                style = TextStyle(
                    brush = shimmerBrush,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif
                ),
                modifier = Modifier
                    .padding(start = 50.dp)
                    .alpha(textAlpha)
            )

            // Metallic Slider Thumb (Knob)
            val thumbOffsetDp = with(density) { dragOffset.value.toDp() }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = thumbOffsetDp)
                    .width(thumbWidth)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(11.dp))
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF6F6F6),
                                Color(0xFFDFDFDF),
                                Color(0xFFBCBCBC),
                                Color(0xFF9E9E9E)
                            )
                        )
                    )
                    .border(
                        width = 0.5.dp,
                        color = Color(0xFF6B6B6B),
                        shape = RoundedCornerShape(11.dp)
                    )
                    .pointerInput(maxDragPx) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (dragOffset.value >= maxDragPx * 0.75f && maxDragPx > 0f) {
                                        // Snap to end and trigger unlock
                                        dragOffset.animateTo(maxDragPx, tween(100))
                                        onUnlock()
                                    } else {
                                        // Bounce back to start with spring
                                        dragOffset.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                coroutineScope.launch {
                                    val newOffset = (dragOffset.value + dragAmount).coerceIn(0f, maxDragPx)
                                    dragOffset.snapTo(newOffset)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Top half glossy shine on knob
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .align(Alignment.TopCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Embossed Arrow symbol
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Unlock arrow",
                        tint = Color(0xFF555555),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
