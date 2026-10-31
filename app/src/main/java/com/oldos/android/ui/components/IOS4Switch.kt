package com.oldos.android.ui.components

import com.oldos.android.ui.theme.IOS4FontFamily
import com.oldos.android.ui.theme.IOS4SwitchOffGradient
import com.oldos.android.ui.theme.IOS4SwitchOnGradient
import com.oldos.android.ui.theme.IOS4SwitchOnOrangeGradient
import com.oldos.android.ui.theme.IOS4SwitchThumbGradient
import com.oldos.android.ui.theme.IOS4SwitchThumbStroke
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IOS4Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    orange: Boolean = false
) {
    val trackWidth = 79.dp
    val trackHeight = 27.dp
    val thumbSize = 27.dp

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "switch_thumb_offset"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .border(width = 0.5.dp, color = IOS4SwitchThumbStroke, shape = RoundedCornerShape(14.dp))
            .background(
                // Inset shadow well beneath the track (approx)
                brush = Brush.verticalGradient(
                    colors = if (checked) {
                        if (orange) IOS4SwitchOnOrangeGradient else IOS4SwitchOnGradient
                    } else {
                        IOS4SwitchOffGradient
                    }
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Text ON on the left
        if (checked) {
            Text(
                text = "ON",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = IOS4FontFamily,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 11.dp)
            )
        } else {
            // Text OFF on the right
            Text(
                text = "OFF",
                color = Color(0xFF6E6E6E),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = IOS4FontFamily,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 9.dp)
            )
        }

        // Sliding metallic thumb
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .shadow(elevation = 3.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to IOS4SwitchThumbGradient[0],
                            0.48f to IOS4SwitchThumbGradient[1],
                            0.52f to IOS4SwitchThumbGradient[2],
                            1f to IOS4SwitchThumbGradient[3]
                        )
                    )
                )
                .border(width = 0.5.dp, color = IOS4SwitchThumbStroke, shape = CircleShape)
        ) {
            // High gloss shine on top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
