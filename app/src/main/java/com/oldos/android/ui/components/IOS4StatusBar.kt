package com.oldos.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.ui.theme.IOS4StatusBarBackground
import com.oldos.android.ui.theme.IOS4StatusBarText
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IOS4StatusBar(
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    carrierName: String = "中国移动",
    batteryPercentage: Int = 100,
    isCharging: Boolean = false
) {
    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(isLocked) {
        if (!isLocked) {
            val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
            while (true) {
                currentTime = formatter.format(Date())
                delay(1000)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(IOS4StatusBarBackground)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Left: Carrier and Signal / Wifi
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Signal Bars (5 classic bars)
            SignalBars(level = 5)

            Text(
                text = carrierName,
                color = IOS4StatusBarText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )

            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Wi-Fi",
                tint = IOS4StatusBarText,
                modifier = Modifier.size(12.dp)
            )
        }

        // Center: Lock icon (on Lock Screen) or Clock (on Home/App)
        if (isLocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = IOS4StatusBarText,
                modifier = Modifier.size(13.dp)
            )
        } else {
            Text(
                text = currentTime,
                color = IOS4StatusBarText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }

        // Right: Battery percentage and battery icon
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$batteryPercentage%",
                color = IOS4StatusBarText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )

            IOS4BatteryIcon(
                percentage = batteryPercentage,
                isCharging = isCharging
            )
        }
    }
}

@Composable
private fun SignalBars(level: Int = 5) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(10.dp)
    ) {
        for (i in 1..5) {
            val barHeight = (i * 2).dp
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(barHeight)
                    .background(
                        if (i <= level) IOS4StatusBarText else IOS4StatusBarText.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

@Composable
private fun IOS4BatteryIcon(
    percentage: Int,
    isCharging: Boolean
) {
    Canvas(modifier = Modifier.size(width = 20.dp, height = 10.dp)) {
        val w = size.width
        val h = size.height
        val bodyWidth = w - 2.5f.dp.toPx()
        val capWidth = 2f.dp.toPx()
        val capHeight = h * 0.45f

        // Battery outer border
        drawRoundRect(
            color = IOS4StatusBarText,
            topLeft = Offset(0f, 0f),
            size = Size(bodyWidth, h),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )

        // Battery terminal cap
        drawRoundRect(
            color = IOS4StatusBarText,
            topLeft = Offset(bodyWidth + 0.5f.dp.toPx(), (h - capHeight) / 2f),
            size = Size(capWidth, capHeight),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )

        // Fill
        val fillPadding = 1.5f.dp.toPx()
        val maxFillWidth = bodyWidth - fillPadding * 2
        val fillWidth = (maxFillWidth * (percentage.coerceIn(0, 100) / 100f)).coerceAtLeast(0f)

        val fillColor = when {
            isCharging -> Color(0xFF4CD964)
            percentage <= 20 -> Color(0xFFFF3B30)
            else -> IOS4StatusBarText
        }

        if (fillWidth > 0) {
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(fillPadding, fillPadding),
                size = Size(fillWidth, h - fillPadding * 2),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }
    }
}
