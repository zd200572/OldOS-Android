package com.oldos.android.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.ui.components.IOS4StatusBar
import com.oldos.android.ui.components.IOS4StatusBarVariant
import com.oldos.android.ui.theme.IOS4FontFamily
import com.oldos.android.ui.theme.IOS4TabBarDarkGradient
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private enum class ClockTab(val label: String) {
    WORLD("World Clock"), ALARM("Alarm"), STOPWATCH("Stopwatch"), TIMER("Timer")
}

/**
 * iOS 4 Clock: black chrome, orange accents.
 * World Clock / Stopwatch / Timer are fully functional; Alarm is a faithful
 * static recreation.
 */
@Composable
fun ClockScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableStateOf(ClockTab.WORLD) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                ClockTab.WORLD -> WorldClockPane()
                ClockTab.ALARM -> AlarmPane()
                ClockTab.STOPWATCH -> StopwatchPane()
                ClockTab.TIMER -> TimerPane()
            }
        }

        ClockTabBar(current = tab, onSelect = { tab = it })
    }
}

@Composable
private fun ClockChrome(
    title: String,
    rightButton: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Light-on-dark header strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(Brush.verticalGradient(listOf(Color(0xFF3A3A3C), Color(0xFF242426)))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = IOS4FontFamily
            )
            if (rightButton != null) {
                Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)) { rightButton() }
            }
        }
        content()
    }
}

// ── World Clock ──────────────────────────────────────────────────────────────

private val worldCities = listOf(
    "Cupertino" to "America/Los_Angeles",
    "New York" to "America/New_York",
    "London" to "Europe/London",
    "Paris" to "Europe/Paris",
    "Moscow" to "Europe/Moscow",
    "Shanghai" to "Asia/Shanghai",
    "Tokyo" to "Asia/Tokyo",
    "Sydney" to "Australia/Sydney"
)

@Composable
private fun WorldClockPane() {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    ClockChrome(title = "World Clock") {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(worldCities) { (city, zone) ->
                val tz = TimeZone.getTimeZone(zone)
                val timeFormat = SimpleDateFormat("h:mm a", Locale.US).apply { timeZone = tz }
                val dayFormat = SimpleDateFormat("EEE", Locale.US).apply { timeZone = tz }
                val cal = java.util.Calendar.getInstance(tz).apply { timeInMillis = now }
                val isDaytime = cal.get(java.util.Calendar.HOUR_OF_DAY) in 6..17

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0E0E0E))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$city, ${dayFormat.format(Date(now))}",
                            color = Color(0xFF9C9CA1),
                            fontSize = 13.sp,
                            fontFamily = IOS4FontFamily
                        )
                    }
                    DayNightGlyph(isDaytime = isDaytime)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = timeFormat.format(Date(now)),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = IOS4FontFamily
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(Color(0xFF2A2A2C))
                )
            }
        }
    }
}

@Composable
private fun DayNightGlyph(isDaytime: Boolean) {
    Canvas(modifier = Modifier.size(22.dp)) {
        if (isDaytime) {
            // Sun: disc + 8 rays
            drawCircle(color = Color(0xFFFFC83B), radius = size.minDimension * 0.26f)
            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45).toDouble())
                val startX = center.x + (Math.cos(angle) * size.minDimension * 0.34f).toFloat()
                val startY = center.y + (Math.sin(angle) * size.minDimension * 0.34f).toFloat()
                val endX = center.x + (Math.cos(angle) * size.minDimension * 0.46f).toFloat()
                val endY = center.y + (Math.sin(angle) * size.minDimension * 0.46f).toFloat()
                drawLine(
                    color = Color(0xFFFFC83B),
                    start = androidx.compose.ui.geometry.Offset(startX, startY),
                    end = androidx.compose.ui.geometry.Offset(endX, endY),
                    strokeWidth = 1.4f
                )
            }
        } else {
            // Crescent moon
            val path = androidx.compose.ui.graphics.Path()
            path.arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height),
                startAngleDegrees = -80f,
                sweepAngleDegrees = 200f,
                forceMoveTo = true
            )
            path.arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    size.width * 0.18f, -size.height * 0.10f,
                    size.width * 1.05f, size.height * 0.92f
                ),
                startAngleDegrees = 150f,
                sweepAngleDegrees = -200f,
                forceMoveTo = false
            )
            path.close()
            drawPath(path, color = Color(0xFFC9C9CE))
        }
    }
}

// ── Alarm ────────────────────────────────────────────────────────────────────

@Composable
private fun AlarmPane() {
    val alarms = remember {
        listOf(
            Triple("7:00 AM", "Weekdays", true),
            Triple("8:30 AM", "Weekends", false),
            Triple("9:15 AM", "Mon, Tue, Wed, Thu, Fri", true)
        )
    }

    ClockChrome(
        title = "Alarm",
        rightButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF89ADEE), Color(0xFF2E7BE5))))
                    .border(0.5.dp, Color(0xFF2D3033), RoundedCornerShape(5.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(text = "+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = IOS4FontFamily)
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            alarms.forEach { (time, label, enabled) ->
                var isOn by remember(time) { mutableStateOf(enabled) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0E0E0E))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = time,
                            color = if (isOn) Color.White else Color(0xFF6B6B70),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = IOS4FontFamily
                        )
                        Text(
                            text = label,
                            color = Color(0xFF9C9CA1),
                            fontSize = 12.sp,
                            fontFamily = IOS4FontFamily
                        )
                    }
                    com.oldos.android.ui.components.IOS4Switch(
                        checked = isOn,
                        onCheckedChange = { isOn = it }
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(Color(0xFF2A2A2C))
                )
            }
        }
    }
}

// ── Stopwatch ────────────────────────────────────────────────────────────────

@Composable
private fun StopwatchPane() {
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var laps by remember { mutableStateOf(listOf<Long>()) }
    var lastTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            lastTick = System.currentTimeMillis()
            while (isRunning) {
                delay(31)
                val nowMs = System.currentTimeMillis()
                elapsedMs += nowMs - lastTick
                lastTick = nowMs
            }
        }
    }

    fun format(ms: Long): String {
        val hundredths = (ms % 1000) / 10
        val seconds = (ms / 1000) % 60
        val minutes = ms / 60000
        return "%02d:%02d.%02d".format(minutes, seconds, hundredths)
    }

    ClockChrome(
        title = "Stopwatch",
        rightButton = {
            IOS4RoundActionButton(text = "Reset") {
                isRunning = false
                elapsedMs = 0L
                laps = emptyList()
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = format(elapsedMs),
                    color = Color.White,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Thin,
                    fontFamily = IOS4FontFamily
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IOS4RoundActionButton(text = "Lap", enabled = isRunning) {
                    laps = laps + elapsedMs
                }
                IOS4StartStopButton(isRunning = isRunning) {
                    isRunning = !isRunning
                }
            }

            // Laps
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .background(Color(0xFF0E0E0E))
            ) {
                items(laps.reversed()) { lap ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Lap ${laps.size - laps.indexOfFirst { it == lap }}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontFamily = IOS4FontFamily,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = format(lap),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontFamily = IOS4FontFamily
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(Color(0xFF2A2A2C))
                    )
                }
            }
        }
    }
}

// ── Timer ────────────────────────────────────────────────────────────────────

@Composable
private fun TimerPane() {
    var remainingMs by remember { mutableLongStateOf(5 * 60_000L) }
    var totalMs by remember { mutableLongStateOf(5 * 60_000L) }
    var isRunning by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var lastTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            lastTick = System.currentTimeMillis()
            while (isRunning) {
                delay(100)
                val nowMs = System.currentTimeMillis()
                remainingMs = (remainingMs - (nowMs - lastTick)).coerceAtLeast(0L)
                lastTick = nowMs
                if (remainingMs == 0L) {
                    isRunning = false
                    finished = true
                }
            }
        }
    }

    fun format(ms: Long): String {
        val totalSeconds = ms / 1000
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    ClockChrome(title = "Timer") {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (finished) "Timer Finished" else format(remainingMs),
                color = if (finished) Color(0xFFFF9500) else Color.White,
                fontSize = 58.sp,
                fontWeight = FontWeight.Thin,
                fontFamily = IOS4FontFamily
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick presets
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(1, 5, 10, 30, 60).forEach { minutes ->
                    val label = if (minutes >= 60) "1 hr" else "$minutes min"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF4A4A4C), Color(0xFF2C2C2E))))
                            .border(0.5.dp, Color(0xFF55555A), RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isRunning) {
                                    totalMs = minutes * 60_000L
                                    remainingMs = totalMs
                                    finished = false
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = IOS4FontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IOS4RoundActionButton(text = "Reset") {
                    isRunning = false
                    finished = false
                    remainingMs = totalMs
                }
                IOS4StartStopButton(
                    isRunning = isRunning,
                    onToggle = {
                        if (!finished || remainingMs > 0L) {
                            finished = false
                            isRunning = !isRunning
                        }
                    }
                )
            }
        }
    }
}

// ── Shared bits ──────────────────────────────────────────────────────────────

@Composable
private fun IOS4RoundActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.4f
    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF4A4A4C), Color(0xFF2C2C2E))
                )
            )
            .border(0.5.dp, Color(0xFF55555A), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = alpha),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = IOS4FontFamily,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun IOS4StartStopButton(
    isRunning: Boolean,
    onToggle: () -> Unit
) {
    val gradient = if (isRunning) {
        listOf(Color(0xFFEF878E), Color(0xFFC7343F), Color(0xFFBD1421))  // Stop = red
    } else {
        listOf(Color(0xFF7DE640), Color(0xFF5AC61F), Color(0xFF45B411))  // Start = green
    }
    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(brush = Brush.verticalGradient(gradient))
            .border(0.5.dp, Color(0x66000000), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isRunning) "Stop" else "Start",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = IOS4FontFamily,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ClockTabBar(
    current: ClockTab,
    onSelect: (ClockTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(IOS4TabBarDarkGradient))
            .padding(top = 6.dp, bottom = 6.dp)
    ) {
        ClockTab.entries.forEach { tabEntry ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(tabEntry) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ClockTabGlyph(tab = tabEntry, selected = current == tabEntry)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tabEntry.label,
                    color = if (current == tabEntry) Color.White else Color(0xFFA8A8A8),
                    fontSize = 9.5.sp,
                    fontFamily = IOS4FontFamily,
                    maxLines = 1
                )
            }
        }
    }
}

/** Simple line-art glyphs for the clock tabs. */
@Composable
private fun ClockTabGlyph(tab: ClockTab, selected: Boolean) {
    val tint = if (selected) Color.White else Color(0xFFA8A8A8)
    Canvas(modifier = Modifier.size(width = 22.dp, height = 20.dp)) {
        val stroke = 1.6.dp.toPx()
        when (tab) {
            ClockTab.WORLD -> {
                drawCircle(tint, radius = size.minDimension * 0.42f, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
                drawLine(tint, Offset(0f, size.height / 2f), Offset(size.width, size.height / 2f), strokeWidth = stroke * 0.8f)
                drawOval(tint, topLeft = Offset(size.width * 0.32f, 0f), size = androidx.compose.ui.geometry.Size(size.width * 0.36f, size.height), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke * 0.8f))
            }
            ClockTab.ALARM -> {
                // Bell
                drawArc(
                    tint,
                    startAngle = 180f, sweepAngle = 180f, useCenter = false,
                    topLeft = Offset(size.width * 0.22f, size.height * 0.22f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.56f, size.height * 0.62f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
                )
                drawLine(tint, Offset(size.width * 0.15f, size.height * 0.62f), Offset(size.width * 0.85f, size.height * 0.62f), strokeWidth = stroke)
                drawLine(tint, Offset(size.width * 0.40f, size.height * 0.80f), Offset(size.width * 0.60f, size.height * 0.80f), strokeWidth = stroke)
            }
            ClockTab.STOPWATCH -> {
                drawCircle(tint, radius = size.minDimension * 0.36f, center = Offset(size.width / 2f, size.height * 0.58f), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
                drawLine(tint, Offset(size.width / 2f, size.height * 0.58f), Offset(size.width * 0.64f, size.height * 0.42f), strokeWidth = stroke * 0.9f)
                drawLine(tint, Offset(size.width * 0.42f, size.height * 0.06f), Offset(size.width * 0.58f, size.height * 0.06f), strokeWidth = stroke)
            }
            ClockTab.TIMER -> {
                // Hourglass-ish: two triangles outline
                drawLine(tint, Offset(size.width * 0.25f, size.height * 0.12f), Offset(size.width * 0.75f, size.height * 0.12f), strokeWidth = stroke)
                drawLine(tint, Offset(size.width * 0.25f, size.height * 0.88f), Offset(size.width * 0.75f, size.height * 0.88f), strokeWidth = stroke)
                drawLine(tint, Offset(size.width * 0.25f, size.height * 0.12f), Offset(size.width / 2f, size.height * 0.5f), strokeWidth = stroke * 0.8f)
                drawLine(tint, Offset(size.width * 0.75f, size.height * 0.12f), Offset(size.width / 2f, size.height * 0.5f), strokeWidth = stroke * 0.8f)
                drawLine(tint, Offset(size.width / 2f, size.height * 0.5f), Offset(size.width * 0.25f, size.height * 0.88f), strokeWidth = stroke * 0.8f)
                drawLine(tint, Offset(size.width / 2f, size.height * 0.5f), Offset(size.width * 0.75f, size.height * 0.88f), strokeWidth = stroke * 0.8f)
            }
        }
    }
}
