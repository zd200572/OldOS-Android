package com.oldos.android.ui.screens

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.oldos.android.ui.components.*
import com.oldos.android.ui.theme.IOS4FontFamily

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar
import java.util.TimeZone

private data class CalendarEvent(
    val id: Long,
    val title: String,
    val beginMs: Long,
    val allDay: Boolean
)

/**
 * iOS 4 Calendar: month grid with a red "today", event dots, and the
 * selected day's agenda below. Reads the real device calendar and inserts
 * new events directly via CalendarContract.
 */
@Composable
fun CalendarScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }
    var events by remember { mutableStateOf<Map<LocalDate, List<CalendarEvent>>>(emptyMap()) }
    var hasPermission by remember { mutableStateOf(hasCalendarPermission(context)) }
    var showAddSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasPermission = grants.values.any { it }
    }

    LaunchedEffect(hasPermission, month) {
        if (hasPermission) {
            events = withContext(Dispatchers.IO) { loadMonthEvents(context, month) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE9ECEF))
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = month.month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + month.year,
            leftButtonText = "Home",
            onLeftButtonClick = onHomeClick,
            rightButtonText = "+",
            onRightButtonClick = { if (hasPermission) showAddSheet = true }
        )

        if (!hasPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Calendar access is required to show your events.",
                        color = Color(0xFF4C566C),
                        fontSize = 14.sp,
                        fontFamily = IOS4FontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.verticalGradient(com.oldos.android.ui.theme.IOS4NavButtonBlueGradient))
                            .clickable { permissionLauncher.launch(calendarPermissions()) }
                            .padding(horizontal = 18.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = "Grant Access",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = IOS4FontFamily
                        )
                    }
                }
            }
        } else {
            // Month navigation bar (dark strip, iOS 4 style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF3A3A3C), Color(0xFF242426))))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthArrow(left = true) { month = month.minusMonths(1) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            month = YearMonth.now()
                            selectedDay = LocalDate.now()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Today",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = IOS4FontFamily
                    )
                }
                MonthArrow(left = false) { month = month.plusMonths(1) }
            }

            // Weekday header
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFD7DBE0))) {
                listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { wd ->
                    Text(
                        text = wd,
                        color = Color(0xFF50555C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = IOS4FontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                    )
                }
            }

            // Month grid
            MonthGrid(
                month = month,
                selectedDay = selectedDay,
                eventDays = events.keys,
                onDayClick = { selectedDay = it }
            )

            // Agenda for the selected day
            Text(
                text = selectedDay.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                color = Color(0xFF4C566C),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = IOS4FontFamily,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )

            val dayEvents = events[selectedDay].orEmpty()
            if (dayEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(vertical = 26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Events",
                        color = Color(0xFF8F8F8F),
                        fontSize = 17.sp,
                        fontFamily = IOS4FontFamily
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(dayEvents) { event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = if (event.allDay) "all-day"
                                else java.time.Instant.ofEpochMilli(event.beginMs)
                                    .atZone(ZoneId.systemDefault())
                                    .format(java.time.format.DateTimeFormatter.ofPattern("h:mm a")),
                                color = Color(0xFF3E5383),
                                fontSize = 13.sp,
                                fontFamily = IOS4FontFamily,
                                modifier = Modifier.width(78.dp)
                            )
                            Text(
                                text = event.title,
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontFamily = IOS4FontFamily,
                                maxLines = 2
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(Color(0xFFE2E2E2))
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AddEventSheet(
                date = selectedDay,
                onDismiss = { showAddSheet = false },
                onAdd = { title ->
                    showAddSheet = false
                    scope.launch {
                        val ok = withContext(Dispatchers.IO) { insertEvent(context, selectedDay, title) }
                        if (ok) {
                            events = withContext(Dispatchers.IO) { loadMonthEvents(context, month) }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selectedDay: LocalDate,
    eventDays: Set<LocalDate>,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayDow = month.atDay(1).dayOfWeek.value % 7  // Sunday = 0
    val daysInMonth = month.lengthOfMonth()
    val today = LocalDate.now()

    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        val cells = listOf(
            List(firstDayDow) { null as LocalDate? } +
                (1..daysInMonth).map { month.atDay(it) }
        ).flatMap { it }
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.15f)
                            .border(0.5.dp, Color(0xFFD7DBE0))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { if (day != null) onDayClick(day) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            val isToday = day == today
                            val isSelected = day == selectedDay
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> Color(0xFFE53935)
                                            isToday -> Color(0xFFD7DBE0)
                                            else -> Color.Transparent
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.dayOfMonth.toString(),
                                    color = when {
                                        isSelected -> Color.White
                                        else -> Color(0xFF3B3B3D)
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = IOS4FontFamily
                                )
                            }
                            // Event dot
                            if (day in eventDays && !isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 5.dp)
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF3B78E7))
                                )
                            }
                        }
                    }
                }
                // Pad the final week out to 7 columns
                repeat(7 - week.size) {
                    Box(modifier = Modifier.weight(1f).aspectRatio(1.15f))
                }
            }
        }
    }
}

@Composable
private fun MonthArrow(left: Boolean, onClick: () -> Unit) {
    Canvas(
        modifier = Modifier
            .size(width = 40.dp, height = 34.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        val stroke = 2.4.dp.toPx()
        val midY = size.height / 2f
        val cx = size.width / 2f
        if (left) {
            drawLine(Color.White, Offset(cx + 5.dp.toPx(), midY - 7.dp.toPx()), Offset(cx - 5.dp.toPx(), midY), strokeWidth = stroke)
            drawLine(Color.White, Offset(cx - 5.dp.toPx(), midY), Offset(cx + 5.dp.toPx(), midY + 7.dp.toPx()), strokeWidth = stroke)
        } else {
            drawLine(Color.White, Offset(cx - 5.dp.toPx(), midY - 7.dp.toPx()), Offset(cx + 5.dp.toPx(), midY), strokeWidth = stroke)
            drawLine(Color.White, Offset(cx + 5.dp.toPx(), midY), Offset(cx - 5.dp.toPx(), midY + 7.dp.toPx()), strokeWidth = stroke)
        }
    }
}

@Composable
private fun AddEventSheet(
    date: LocalDate,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xF2FFFFFF))
            .clickable(enabled = false) {}
            .padding(16.dp)
    ) {
        Text(
            text = "New Event — " + date.format(java.time.format.DateTimeFormatter.ofPattern("MMM d")),
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = IOS4FontFamily
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF0F0F2))
                .border(0.5.dp, Color(0xFFABABAB), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            if (title.isEmpty()) {
                Text(
                    text = "Title",
                    color = Color(0xFF8F8F8F),
                    fontSize = 15.sp,
                    fontFamily = IOS4FontFamily
                )
            }
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.Black, fontSize = 15.sp, fontFamily = IOS4FontFamily),
                cursorBrush = SolidColor(Color(0xFF3B78E7)),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            IOS4NavButton(text = "Cancel", onClick = onDismiss)
            IOS4NavButton(
                text = "Add",
                tinted = false,
                onClick = { if (title.isNotBlank()) onAdd(title.trim()) }
            )
        }
    }
}

// ── Data ─────────────────────────────────────────────────────────────────────

private fun hasCalendarPermission(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
        PackageManager.PERMISSION_GRANTED

private fun calendarPermissions() = arrayOf(
    Manifest.permission.READ_CALENDAR,
    Manifest.permission.WRITE_CALENDAR
)

private fun loadMonthEvents(
    context: android.content.Context,
    month: YearMonth
): Map<LocalDate, List<CalendarEvent>> {
    val zone = ZoneId.systemDefault()
    val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()

    val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
        .appendPath(start.toString())
        .appendPath(end.toString())
        .build()

    val result = mutableMapOf<LocalDate, MutableList<CalendarEvent>>()
    try {
        context.contentResolver.query(
            uri,
            arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.ALL_DAY
            ),
            null,
            null,
            CalendarContract.Instances.BEGIN + " ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val beginMs = cursor.getLong(2)
                val day = java.time.Instant.ofEpochMilli(beginMs).atZone(zone).toLocalDate()
                result.getOrPut(day) { mutableListOf() } += CalendarEvent(
                    id = cursor.getLong(0),
                    title = cursor.getString(1) ?: "(No title)",
                    beginMs = beginMs,
                    allDay = cursor.getInt(3) != 0
                )
            }
        }
    } catch (_: Exception) {
    }
    return result
}

private fun insertEvent(
    context: android.content.Context,
    day: LocalDate,
    title: String
): Boolean {
    return try {
        val calId = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(CalendarContract.Calendars._ID),
            null, null, null
        )?.use { if (it.moveToFirst()) it.getLong(0) else null } ?: return false

        val zone = ZoneId.systemDefault()
        val start = day.atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
        val end = day.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()

        val values = android.content.ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DTSTART, start)
            put(CalendarContract.Events.DTEND, end)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values) != null
    } catch (e: SecurityException) {
        false
    } catch (_: Exception) {
        false
    }
}
