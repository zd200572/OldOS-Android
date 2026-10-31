package com.oldos.android.ui.screens

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.ui.components.IOS4NavigationBar
import com.oldos.android.ui.components.IOS4StatusBar
import com.oldos.android.ui.components.IOS4NavigationBar
import com.oldos.android.ui.components.IOS4StatusBarVariant
import com.oldos.android.ui.theme.IOS4FontFamily
import kotlin.math.atan2
import kotlin.math.roundToInt

/**
 * iOS 4 Compass: live heading from the accelerometer + magnetometer, with the
 * rotating dial, red/white needle and cardinal letters.
 */
@Composable
fun CompassScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var heading by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        var gravity = FloatArray(3)
        var geomagnetic = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        geomagnetic = event.values.clone()
                        val rotation = FloatArray(9)
                        val inclination = FloatArray(9)
                        if (SensorManager.getRotationMatrix(rotation, inclination, gravity, geomagnetic)) {
                            val orientation = FloatArray(3)
                            SensorManager.getOrientation(rotation, orientation)
                            var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            azimuth = (azimuth + 360f) % 360f
                            // Smooth the needle
                            heading = heading + 0.15f * shortestAngleDelta(heading, azimuth)
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val cardinal = when (((heading % 360f) + 360f) % 360f) {
        in 315f..360f, in 0f..45f -> "N"
        in 45f..135f -> "E"
        in 135f..225f -> "S"
        else -> "W"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = "Compass",
            leftButtonText = "Home",
            onLeftButtonClick = onHomeClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF101012), Color(0xFF000000))
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "${heading.roundToInt()}° $cardinal",
                color = Color(0xFFFF9500),
                fontSize = 46.sp,
                fontWeight = FontWeight.Thin,
                fontFamily = IOS4FontFamily
            )

            Spacer(modifier = Modifier.weight(0.4f))

            // Compass dial
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .aspectRatio(1f)
            ) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Dial face
                drawCircle(color = Color(0xFF151517), radius = radius, center = center)
                drawCircle(
                    color = Color(0xFF4A4A4E),
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx())
                )

                // Ticks around the dial (rotating with heading)
                for (deg in 0 until 360 step 15) {
                    val angle = Math.toRadians((deg - heading + 90.0) % 360.0)
                    val isMajor = deg % 45 == 0
                    val inner = if (isMajor) 0.72f else 0.80f
                    drawLine(
                        color = if (isMajor) Color.White else Color(0xFF6B6B70),
                        start = Offset(
                            center.x + (kotlin.math.cos(angle) * radius * inner).toFloat(),
                            center.y + (kotlin.math.sin(angle) * radius * inner).toFloat()
                        ),
                        end = Offset(
                            center.x + (kotlin.math.cos(angle) * radius * 0.86f).toFloat(),
                            center.y + (kotlin.math.sin(angle) * radius * 0.86f).toFloat()
                        ),
                        strokeWidth = if (isMajor) 2.4f else 1.4f
                    )
                }

                // Cardinal letters (rotating: N always at the dial's north)
                // Draw the red needle pointing north
                val northAngle = Math.toRadians((-heading + 90.0) % 360.0)
                val needleLength = radius * 0.66f
                val needleHalf = 4.dp.toPx()

                val tipX = center.x + (kotlin.math.cos(northAngle) * needleLength).toFloat()
                val tipY = center.y + (kotlin.math.sin(northAngle) * needleLength).toFloat()
                val tailX = center.x - (kotlin.math.cos(northAngle) * needleLength * 0.72f).toFloat()
                val tailY = center.y - (kotlin.math.sin(northAngle) * needleLength * 0.72f).toFloat()

                // Perpendicular for needle width
                val perpX = -kotlin.math.sin(northAngle).toFloat() * needleHalf
                val perpY = kotlin.math.cos(northAngle).toFloat() * needleHalf

                // Red half (points north)
                val redPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(tipX, tipY)
                    lineTo(center.x + perpX, center.y + perpY)
                    lineTo(center.x - perpX, center.y - perpY)
                    close()
                }
                drawPath(redPath, color = Color(0xFFE53935))

                // White half
                val whitePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(tailX, tailY)
                    lineTo(center.x + perpX, center.y + perpY)
                    lineTo(center.x - perpX, center.y - perpY)
                    close()
                }
                drawPath(whitePath, color = Color(0xFFE8E8EA))

                drawCircle(color = Color(0xFFC9C9CE), radius = 4.dp.toPx(), center = center)
            }

            Spacer(modifier = Modifier.weight(0.15f))

            // Cardinal letters row
            Text(
                text = "N      E      S      W",
                color = Color(0xFF9C9CA1),
                fontSize = 16.sp,
                fontFamily = IOS4FontFamily,
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.weight(0.1f))
        }
    }
}

private fun shortestAngleDelta(from: Float, to: Float): Float {
    var delta = (to - from) % 360f
    if (delta > 180f) delta -= 360f
    if (delta < -180f) delta += 360f
    return delta
}
