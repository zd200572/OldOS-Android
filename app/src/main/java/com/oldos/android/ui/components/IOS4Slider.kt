package com.oldos.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * iOS 4 UISlider: recessed dark groove track, glossy round thumb.
 * Dimensions per nos4 packages/UIKit/src/Slider/UISliderMetrics.ts.
 */
@Composable
fun IOS4Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var trackWidthPx by remember { mutableFloatStateOf(1f) }
    val thumbSize = 26.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbSize)
            .onSizeChanged { trackWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(value, trackWidthPx) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / (trackWidthPx)).coerceIn(0f, 1f)
                    onValueChange(fraction)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Recessed groove
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .padding(horizontal = (thumbSize.value / 2).dp)
                .clip(RoundedCornerShape(4.5.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF1A1A1A), Color(0xFF4A4A4A), Color(0xFF2E2E2E))
                    )
                )
                .border(0.5.dp, Color(0x66000000), RoundedCornerShape(4.5.dp))
        )

        // Glossy thumb
        val travelPx = with(density) { (trackWidthPx - thumbSize.toPx()).coerceAtLeast(0f) }
        Box(
            modifier = Modifier
                .offset(x = with(density) { (travelPx * value.coerceIn(0f, 1f)).toDp() })
                .size(thumbSize)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color.White, Color(0xFFE8E8E8), Color(0xFFCFCFCF))
                    )
                )
                .border(0.5.dp, Color(0xFF8C8C8C), CircleShape)
                .pointerInput(trackWidthPx) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val delta = dragAmount / travelPx
                            onValueChange((value + delta).coerceIn(0f, 1f))
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Thumb gloss
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )
        }
    }
}
