package com.oldos.android.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.model.*
import com.oldos.android.ui.components.IOS4StatusBar
import com.oldos.android.ui.components.IOS4StatusBarVariant
import com.oldos.android.ui.theme.IOS4FontFamily
import kotlinx.coroutines.launch

/**
 * iOS 4 Weather: deep blue gradient sky, city/temp header, hourly strip and
 * six-day forecast. Live data from Open-Meteo with graceful offline fallback.
 */
@Composable
fun WeatherScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cityIndex by remember { mutableIntStateOf(0) }
    var data by remember { mutableStateOf<WeatherData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun load() {
        isLoading = true
        scope.launch {
            data = WeatherRepository.fetch(weatherCities[cityIndex])
            isLoading = false
        }
    }

    LaunchedEffect(cityIndex) { load() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF4A8FD4), Color(0xFF2B6CB0), Color(0xFF174A86))
                )
            )
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)

        // Nav bar: light blue translucent, city cycler
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color(0x33000000)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CycleArrow(direction = -1) {
                if (!isLoading) cityIndex = (cityIndex - 1 + weatherCities.size) % weatherCities.size
            }
            Text(
                text = weatherCities[cityIndex].name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = IOS4FontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            CycleArrow(direction = 1) {
                if (!isLoading) cityIndex = (cityIndex + 1) % weatherCities.size
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Current conditions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = weatherCities[cityIndex].name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = IOS4FontFamily,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (data != null) "${data!!.temperatureC.toInt()}°" else "",
                    color = Color.White,
                    fontSize = 74.sp,
                    fontWeight = FontWeight.Thin,
                    fontFamily = IOS4FontFamily
                )
                Text(
                    text = if (data != null) WeatherRepository.conditionText(data!!.weatherCode)
                    else if (isLoading) "Loading…" else "Not Available",
                    color = Color(0xFFCFE4F7),
                    fontSize = 15.sp,
                    fontFamily = IOS4FontFamily
                )
                if (data != null) {
                    Text(
                        text = "H: ${data!!.daily.firstOrNull()?.maxC?.toInt() ?: "—"}°  L: ${data!!.daily.firstOrNull()?.minC?.toInt() ?: "—"}°   Wind ${data!!.windSpeed.toInt()} km/h",
                        color = Color(0xFFCFE4F7),
                        fontSize = 13.sp,
                        fontFamily = IOS4FontFamily
                    )
                }
            }

            // Hourly strip
            if (data != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x26000000))
                        .padding(vertical = 10.dp)
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(data!!.hourly) { hour ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = hour.hourLabel,
                                    color = Color(0xFFCFE4F7),
                                    fontSize = 12.sp,
                                    fontFamily = IOS4FontFamily
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                WeatherGlyph(code = hour.weatherCode, glyphSize = 26.dp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${hour.temperatureC.toInt()}°",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = IOS4FontFamily
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6-day forecast
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .background(Color(0x26000000), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        data!!.daily.forEachIndexed { index, day ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = day.dayLabel,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = IOS4FontFamily,
                                    modifier = Modifier.weight(1.2f)
                                )
                                WeatherGlyph(code = day.weatherCode, glyphSize = 24.dp)
                                Text(
                                    text = "${day.minC.toInt()}°",
                                    color = Color(0xFFB9D4EC),
                                    fontSize = 17.sp,
                                    fontFamily = IOS4FontFamily,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${day.maxC.toInt()}°",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontFamily = IOS4FontFamily,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (index < data!!.daily.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .background(Color(0x33FFFFFF))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Powered by Open-Meteo",
                    color = Color(0x88FFFFFF),
                    fontSize = 10.sp,
                    fontFamily = IOS4FontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun CycleArrow(direction: Int, onClick: () -> Unit) {
    Canvas(
        modifier = Modifier
            .size(width = 40.dp, height = 44.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        val stroke = 2.4.dp.toPx()
        val midY = size.height / 2f
        val cx = size.width / 2f
        if (direction < 0) {
            drawLine(Color.White, Offset(cx + 4.dp.toPx(), midY - 6.dp.toPx()), Offset(cx - 4.dp.toPx(), midY), strokeWidth = stroke)
            drawLine(Color.White, Offset(cx - 4.dp.toPx(), midY), Offset(cx + 4.dp.toPx(), midY + 6.dp.toPx()), strokeWidth = stroke)
        } else {
            drawLine(Color.White, Offset(cx - 4.dp.toPx(), midY - 6.dp.toPx()), Offset(cx + 4.dp.toPx(), midY), strokeWidth = stroke)
            drawLine(Color.White, Offset(cx + 4.dp.toPx(), midY), Offset(cx - 4.dp.toPx(), midY + 6.dp.toPx()), strokeWidth = stroke)
        }
    }
}

/** Hand-drawn weather glyphs: sun, cloud, rain, snow, bolt. */
@Composable
fun WeatherGlyph(code: Int, glyphSize: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.size(width = glyphSize, height = glyphSize)) {
        val w = size.width
        val h = size.height
        val sunColor = Color(0xFFFFD24A)
        val cloudColor = Color(0xFFF2F2F2)
        val darkCloud = Color(0xFFBFC7CE)

        val isSnow = code in 71..77 || code in 85..86
        val isRain = code in 51..67 || code in 80..82
        val isStorm = code >= 95
        val isFog = code == 45 || code == 48
        val isCloudy = code == 3
        val isPartly = code == 1 || code == 2

        if (code == 0 || isPartly) {
            // Sun with rays
            drawCircle(sunColor, radius = w * (if (isPartly) 0.18f else 0.26f), center = Offset(w * (if (isPartly) 0.32f else 0.5f), h * (if (isPartly) 0.32f else 0.5f)))
            if (!isPartly) {
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45).toDouble())
                    drawLine(
                        sunColor,
                        Offset(
                            (w * 0.5f + Math.cos(angle) * w * 0.34f).toFloat(),
                            (h * 0.5f + Math.sin(angle) * h * 0.34f).toFloat()
                        ),
                        Offset(
                            (w * 0.5f + Math.cos(angle) * w * 0.46f).toFloat(),
                            (h * 0.5f + Math.sin(angle) * h * 0.46f).toFloat()
                        ),
                        strokeWidth = 1.6f
                    )
                }
            }
        }

        if (isPartly || isCloudy || isRain || isSnow || isStorm) {
            // Cloud: three overlapping circles + base
            val cloudTop = if (isPartly) h * 0.42f else h * 0.28f
            drawCircle(if (isStorm) darkCloud else cloudColor, radius = w * 0.16f, center = Offset(w * 0.34f, cloudTop + h * 0.10f))
            drawCircle(if (isStorm) darkCloud else cloudColor, radius = w * 0.20f, center = Offset(w * 0.54f, cloudTop + h * 0.06f))
            drawCircle(if (isStorm) darkCloud else cloudColor, radius = w * 0.16f, center = Offset(w * 0.70f, cloudTop + h * 0.12f))
            drawRoundRect(
                if (isStorm) darkCloud else cloudColor,
                topLeft = Offset(w * 0.20f, cloudTop + h * 0.12f),
                size = androidx.compose.ui.geometry.Size(w * 0.62f, h * 0.18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.09f, w * 0.09f)
            )
        }

        if (isRain || isStorm) {
            for (i in 0 until 3) {
                val x = w * (0.32f + i * 0.18f)
                drawLine(
                    Color(0xFF9BD1F5),
                    Offset(x, h * 0.66f),
                    Offset(x - w * 0.04f, h * 0.86f),
                    strokeWidth = 1.8f
                )
            }
        }
        if (isStorm) {
            val bolt = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.52f, h * 0.60f)
                lineTo(w * 0.42f, h * 0.76f)
                lineTo(w * 0.49f, h * 0.76f)
                lineTo(w * 0.44f, h * 0.94f)
                lineTo(w * 0.58f, h * 0.74f)
                lineTo(w * 0.51f, h * 0.74f)
                close()
            }
            drawPath(bolt, color = Color(0xFFFFC83B))
        }
        if (isSnow) {
            for (i in 0 until 3) {
                drawCircle(
                    Color.White,
                    radius = w * 0.035f,
                    center = Offset(w * (0.34f + i * 0.17f), h * (0.72f + (i % 2) * 0.12f))
                )
            }
        }
        if (isFog) {
            for (i in 0 until 3) {
                drawLine(
                    Color(0xFFCFD8DE),
                    Offset(w * 0.18f, h * (0.55f + i * 0.12f)),
                    Offset(w * 0.82f, h * (0.55f + i * 0.12f)),
                    strokeWidth = 1.6f
                )
            }
        }
    }
}
