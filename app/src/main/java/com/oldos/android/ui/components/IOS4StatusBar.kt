package com.oldos.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oldos.android.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*


/** Overlay = translucent dark (springboard/lock screen). Light = in-app gray gradient. */
enum class IOS4StatusBarVariant { Overlay, Light }

@Composable
fun IOS4StatusBar(
    modifier: Modifier = Modifier,
    variant: IOS4StatusBarVariant = IOS4StatusBarVariant.Overlay,
    isLocked: Boolean = false,
    carrierName: String? = null,
    batteryPercentage: Int? = null,
    isCharging: Boolean? = null
) {
    var currentTime by remember { mutableStateOf("") }
    val battery = rememberBatteryStatus()
    val effectiveBattery = batteryPercentage ?: battery.percentage
    val effectiveCharging = isCharging ?: battery.isCharging
    val effectiveCarrier = rememberCarrierName(
        fallback = com.oldos.android.model.SettingsManager.carrierName
    )
    val is24Hour = com.oldos.android.model.SettingsManager.is24HourTime

    LaunchedEffect(isLocked, is24Hour) {
        if (!isLocked) {
            val formatter = SimpleDateFormat(if (is24Hour) "HH:mm" else "h:mm a", Locale.US)
            while (true) {
                currentTime = formatter.format(Date())
                delay(1000)
            }
        }
    }

    // Swallow the camera cutout: keep the chrome seamless by growing the band
    val density = LocalDensity.current
    val cutoutTop = WindowInsets.displayCutout.getTop(density)
    val cutoutHeight = with(density) { cutoutTop.toDp() }
    val bandHeight = if (cutoutHeight > 20.dp) cutoutHeight else 20.dp

    val timeColor: Color
    val carrierColor: Color
    val glyphColor: Color
    val textStyle: TextStyle
    when (variant) {
        IOS4StatusBarVariant.Light -> {
            timeColor = IOS4StatusBarLightTime
            carrierColor = IOS4StatusBarLightCarrier
            glyphColor = IOS4StatusBarLightGlyph
            // In-app text carries a white emboss shadow: 0 1px 0 rgba(255,255,255,0.5)
            textStyle = IOS4StatusBarTimeStyle.copy(
                shadow = Shadow(
                    color = IOS4StatusBarLightTextShadow,
                    offset = Offset(0f, 1f),
                    blurRadius = 0f
                )
            )
        }
        IOS4StatusBarVariant.Overlay -> {
            timeColor = IOS4StatusBarDarkText
            carrierColor = IOS4StatusBarDarkText
            glyphColor = IOS4StatusBarDarkText
            textStyle = IOS4StatusBarTimeStyle
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bandHeight)
            .then(
                when (variant) {
                    IOS4StatusBarVariant.Light -> Modifier.drawBehind {
                        val w = size.width
                        val h = size.height
                        // Light chrome gradient: solid top 7%, falls to bottom gray
                        drawRect(
                            brush = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to IOS4StatusBarLightGradientTop,
                                    0.07f to IOS4StatusBarLightGradientTop,
                                    1f to IOS4StatusBarLightGradientBottom
                                )
                            )
                        )
                        // Inner bottom shadow 1.8px + edge line 0.45px
                        drawRect(
                            color = IOS4StatusBarLightInnerShadow,
                            topLeft = Offset(0f, h - 2.25f),
                            size = Size(w, 1.8f)
                        )
                        drawRect(
                            color = IOS4StatusBarLightEdge,
                            topLeft = Offset(0f, h - 0.45f),
                            size = Size(w, 0.45f)
                        )
                    }
                    IOS4StatusBarVariant.Overlay -> Modifier.background(IOS4StatusBarDarkBackground)
                }
            )
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Left: signal, carrier, Wi-Fi
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IOS4SignalBars(level = 5, color = glyphColor)

            Text(
                text = effectiveCarrier,
                color = carrierColor,
                style = IOS4StatusBarTextStyle
            )

            IOS4WifiGlyph(color = glyphColor)
        }

        // Center: lock (locked) or clock
        if (isLocked) {
            IOS4LockGlyph(color = glyphColor)
        } else {
            Text(
                text = currentTime,
                color = timeColor,
                style = textStyle
            )
        }

        // Right: battery percentage + battery glyph
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$effectiveBattery%",
                color = glyphColor,
                style = IOS4StatusBarAuxStyle
            )

            IOS4BatteryGlyph(
                percentage = effectiveBattery,
                isCharging = effectiveCharging,
                color = glyphColor
            )
        }
    }
}

/** Five ascending rounded signal bars (iOS 4 cellular indicator). */
@Composable
private fun IOS4SignalBars(level: Int, color: Color) {
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
                        color = if (i <= level) color else color.copy(alpha = 0.3f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.75.dp)
                    )
            )
        }
    }
}

/** Wi-Fi fan: dot + two arcs, hand-drawn to replace the Material glyph. */
@Composable
private fun IOS4WifiGlyph(color: Color) {
    Canvas(modifier = Modifier.size(width = 12.dp, height = 9.dp)) {
        val stroke = 1.4.dp.toPx()
        val cx = size.width / 2f
        val baseY = size.height - stroke / 2f

        drawCircle(
            color = color,
            radius = 0.9.dp.toPx(),
            center = Offset(cx, baseY - 0.9.dp.toPx())
        )
        drawArc(
            color = color,
            startAngle = -135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - 3.4.dp.toPx(), baseY - 3.4.dp.toPx()),
            size = Size(6.8f.dp.toPx(), 6.8f.dp.toPx()),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = -135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - 6.2.dp.toPx(), baseY - 6.2.dp.toPx()),
            size = Size(12.4f.dp.toPx(), 12.4f.dp.toPx()),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

/** iOS 4 padlock shown in the status bar while the device is locked. */
@Composable
private fun IOS4LockGlyph(color: Color) {
    Canvas(modifier = Modifier.size(width = 13.dp, height = 13.dp)) {
        val w = size.width
        val h = size.height
        val stroke = 1.4.dp.toPx()

        // Shackle
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.30f, h * 0.06f),
            size = Size(w * 0.40f, w * 0.40f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // Body
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.18f, h * 0.40f),
            size = Size(w * 0.64f, h * 0.50f),
            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
        )
    }
}

/** Battery glyph with the classic 4-stop green fill gradient and charge bolt. */
@Composable
private fun IOS4BatteryGlyph(
    percentage: Int,
    isCharging: Boolean,
    color: Color
) {
    Canvas(modifier = Modifier.size(width = 20.dp, height = 10.dp)) {
        val w = size.width
        val h = size.height
        val bodyWidth = w - 2.5f.dp.toPx()
        val capWidth = 2f.dp.toPx()
        val capHeight = h * 0.45f

        // Shell
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, 0f),
            size = Size(bodyWidth, h),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )

        // Terminal cap
        drawRoundRect(
            color = color,
            topLeft = Offset(bodyWidth + 0.5f.dp.toPx(), (h - capHeight) / 2f),
            size = Size(capWidth, capHeight),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )

        // Fill
        val fillPadding = 1.5f.dp.toPx()
        val maxFillWidth = bodyWidth - fillPadding * 2
        val fillWidth = (maxFillWidth * (percentage.coerceIn(0, 100) / 100f)).coerceAtLeast(0f)

        if (fillWidth > 0) {
            when {
                isCharging -> drawRoundRect(
                    color = Color(0xFF4CD964),
                    topLeft = Offset(fillPadding, fillPadding),
                    size = Size(fillWidth, h - fillPadding * 2),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
                percentage <= 20 -> drawRoundRect(
                    color = IOS4BatteryLow,
                    topLeft = Offset(fillPadding, fillPadding),
                    size = Size(fillWidth, h - fillPadding * 2),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
                else -> drawRoundRect(
                    brush = Brush.verticalGradient(IOS4BatteryFillGradient),
                    topLeft = Offset(fillPadding, fillPadding),
                    size = Size(fillWidth, h - fillPadding * 2),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
        }

        // Charging bolt
        if (isCharging) {
            val bolt = Path().apply {
                moveTo(w * 0.52f, h * 0.10f)
                lineTo(w * 0.36f, h * 0.55f)
                lineTo(w * 0.47f, h * 0.55f)
                lineTo(w * 0.42f, h * 0.92f)
                lineTo(w * 0.62f, h * 0.42f)
                lineTo(w * 0.51f, h * 0.42f)
                lineTo(w * 0.60f, h * 0.10f)
                close()
            }
            drawPath(bolt, color = Color.White)
        }
    }
}
