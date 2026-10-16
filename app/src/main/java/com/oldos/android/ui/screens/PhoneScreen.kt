package com.oldos.android.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.oldos.android.R
import com.oldos.android.data.RealCallEntry
import com.oldos.android.data.RealContact
import com.oldos.android.data.SystemDataRepository
import com.oldos.android.ui.components.IOS4NavigationBar
import com.oldos.android.ui.components.IOS4StatusBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

enum class PhoneTab {
    FAVORITES,
    RECENTS,
    CONTACTS,
    KEYPAD,
    VOICEMAIL
}

@Composable
fun PhoneScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(PhoneTab.KEYPAD) }
    var rawPhoneNumber by remember { mutableStateOf("") }

    // ── Real data state ───────────────────────────────────────────────────────
    val recentCallsList = remember { mutableStateListOf<RealCallEntry>() }
    val contactsList    = remember { mutableStateListOf<RealContact>() }
    var permissionsGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }

    // ── Haptics & DTMF Audio generator ───────────────────────────────────────
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val toneGen = remember {
        try { ToneGenerator(AudioManager.STREAM_VOICE_CALL, 75) } catch (_: Exception) { null }
    }

    DisposableEffect(Unit) { onDispose { try { toneGen?.release() } catch (_: Exception) {} } }

    fun playToneAndVibrate(toneType: Int) {
        try { toneGen?.startTone(toneType, 120) } catch (_: Exception) {}
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            else @Suppress("DEPRECATION") vibrator?.vibrate(20L)
        } catch (_: Exception) {}
    }

    // ── Load real data ────────────────────────────────────────────────────────
    fun loadRealData() {
        val hasCallLog = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        val hasContacts = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        if (hasCallLog || hasContacts) {
            permissionsGranted = true
            coroutineScope.launch {
                val calls = withContext(Dispatchers.IO) { SystemDataRepository.readCallLog(context, 100) }
                val contacts = withContext(Dispatchers.IO) { SystemDataRepository.readContacts(context) }
                recentCallsList.clear()
                recentCallsList.addAll(calls)
                contactsList.clear()
                contactsList.addAll(contacts)
            }
        }
    }

    // ── Permission launcher ───────────────────────────────────────────────────
    val multiPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.READ_CALL_LOG] == true ||
            grants[Manifest.permission.READ_CONTACTS] == true
        ) {
            permissionsGranted = true
            loadRealData()
        }
        // If CALL_PHONE was pending make the call now
        if (rawPhoneNumber.isNotBlank()) {
            val hasCall = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) ==
                    PackageManager.PERMISSION_GRANTED
            val intent = Intent(
                if (hasCall) Intent.ACTION_CALL else Intent.ACTION_DIAL,
                Uri.parse("tel:$rawPhoneNumber")
            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            context.startActivity(intent)
        }
    }

    // ── Initial load & tab-switch reload ──────────────────────────────────────
    LaunchedEffect(currentTab) {
        val hasCallLog = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        val hasContacts = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

        if (hasCallLog || hasContacts) {
            loadRealData()
        } else {
            multiPermLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.CALL_PHONE
                )
            )
        }
    }

    // ── Make call ─────────────────────────────────────────────────────────────
    fun makeCall(number: String) {
        if (number.isBlank()) return
        val clean = number.replace(" ", "").replace("-", "").replace("(", "").replace(")", "")
        val hasCallPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) ==
                PackageManager.PERMISSION_GRANTED

        if (hasCallPerm) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$clean")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } else {
            rawPhoneNumber = clean
            multiPermLauncher.launch(arrayOf(Manifest.permission.CALL_PHONE))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IOS4StatusBar()

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (currentTab) {
                PhoneTab.KEYPAD -> KeypadTabView(
                    phoneNumber = rawPhoneNumber,
                    onNumberChange = { rawPhoneNumber = it },
                    onDigitPress = { digit, tone ->
                        rawPhoneNumber += digit
                        playToneAndVibrate(tone)
                    },
                    onBackspace = {
                        if (rawPhoneNumber.isNotEmpty()) {
                            rawPhoneNumber = rawPhoneNumber.dropLast(1)
                            playToneAndVibrate(ToneGenerator.TONE_PROP_BEEP2)
                        }
                    },
                    onClearAll = { rawPhoneNumber = "" },
                    onCallClick = { makeCall(rawPhoneNumber) }
                )
                PhoneTab.RECENTS -> RecentsTabView(
                    recents = recentCallsList,
                    hasPermission = permissionsGranted,
                    onCallContact = { makeCall(it) },
                    onRequestPermission = {
                        multiPermLauncher.launch(
                            arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS)
                        )
                    }
                )
                PhoneTab.FAVORITES -> FavoritesTabView(
                    favorites = contactsList,
                    onCallContact = { makeCall(it) }
                )
                PhoneTab.CONTACTS -> ContactsTabView(
                    contacts = contactsList,
                    hasPermission = permissionsGranted,
                    onCallContact = { makeCall(it) },
                    onRequestPermission = {
                        multiPermLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS))
                    }
                )
                PhoneTab.VOICEMAIL -> VoicemailTabView(onCallVoicemail = { makeCall("10086") })
            }
        }

        IOS4PhoneTabBar(currentTab = currentTab, onTabSelected = { currentTab = it })
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Keypad Tab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun KeypadTabView(
    phoneNumber: String,
    onNumberChange: (String) -> Unit,
    onDigitPress: (String, Int) -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onCallClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF263C57), Color(0xFF14243A), Color(0xFF0A1523))
                )
            )
    ) {
        // LCD Number Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF5B718D), Color(0xFF2A4261), Color(0xFF142742), Color(0xFF091628))
                    )
                )
                .border(width = 0.5.dp, color = Color(0xFF040A12))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = phoneNumber,
                color = Color.White,
                fontSize = if (phoneNumber.length > 12) 26.sp else 34.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), offset = Offset(0f, -1.5f), blurRadius = 2f)
                )
            )
        }

        // Keypad Grid
        val keys = listOf(
            listOf(Triple("1", "", ToneGenerator.TONE_DTMF_1), Triple("2", "ABC", ToneGenerator.TONE_DTMF_2), Triple("3", "DEF", ToneGenerator.TONE_DTMF_3)),
            listOf(Triple("4", "GHI", ToneGenerator.TONE_DTMF_4), Triple("5", "JKL", ToneGenerator.TONE_DTMF_5), Triple("6", "MNO", ToneGenerator.TONE_DTMF_6)),
            listOf(Triple("7", "PQRS", ToneGenerator.TONE_DTMF_7), Triple("8", "TUV", ToneGenerator.TONE_DTMF_8), Triple("9", "WXYZ", ToneGenerator.TONE_DTMF_9)),
            listOf(Triple("*", "", ToneGenerator.TONE_DTMF_S), Triple("0", "+", ToneGenerator.TONE_DTMF_0), Triple("#", "", ToneGenerator.TONE_DTMF_P))
        )

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 1.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            for (row in keys) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for ((digit, sub, tone) in row) {
                        KeypadButton(
                            digit = digit,
                            subLetters = sub,
                            onPress = { onDigitPress(digit, tone) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Bottom action row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add to Contact
                Box(
                    modifier = Modifier.weight(1f).height(58.dp).padding(horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (phoneNumber.isNotEmpty()) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_phone_add_contact),
                            contentDescription = "Add Contact",
                            modifier = Modifier.size(48.dp).clickable { }
                        )
                    }
                }

                // Green Call Button
                Box(
                    modifier = Modifier
                        .weight(1.35f).height(56.dp).padding(horizontal = 4.dp)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF7DE640), Color(0xFF56C91D), Color(0xFF389D0C))
                            )
                        )
                        .border(width = 0.8.dp, color = Color(0xFF277206), shape = RoundedCornerShape(8.dp))
                        .clickable { onCallClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(26.dp).align(Alignment.TopCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)
                                )
                            )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Image(painter = painterResource(id = R.drawable.ic_phone_callglyph), contentDescription = "Call", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Call",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            style = TextStyle(shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), offset = Offset(0f, -1f)))
                        )
                    }
                }

                // Backspace
                Box(
                    modifier = Modifier.weight(1f).height(58.dp).padding(horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (phoneNumber.isNotEmpty()) {
                        Box(
                            modifier = Modifier.size(48.dp).pointerInput(Unit) {
                                detectTapGestures(onTap = { onBackspace() }, onLongPress = { onClearAll() })
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(painter = painterResource(id = R.drawable.ic_phone_backspace), contentDescription = "Backspace", modifier = Modifier.size(36.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(digit: String, subLetters: String, onPress: () -> Unit, modifier: Modifier = Modifier) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(58.dp)
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                brush = if (isPressed)
                    Brush.verticalGradient(colors = listOf(Color(0xFF1D2E44), Color(0xFF2C4362)))
                else
                    Brush.verticalGradient(colors = listOf(Color(0xFF6E8199), Color(0xFF4C6584), Color(0xFF334F70)))
            )
            .border(width = 0.5.dp, color = Color(0xFF1B2E46), shape = RoundedCornerShape(6.dp))
            .pointerInput(Unit) {
                detectTapGestures(onPress = { isPressed = true; onPress(); tryAwaitRelease(); isPressed = false })
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            when (digit) {
                "*" -> Image(painter = painterResource(id = R.drawable.ic_phone_star), contentDescription = "*", modifier = Modifier.size(24.dp))
                "#" -> Image(painter = painterResource(id = R.drawable.ic_phone_pound), contentDescription = "#", modifier = Modifier.size(22.dp))
                else -> Text(
                    text = digit,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    style = TextStyle(shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), offset = Offset(0f, -1f)))
                )
            }
            if (subLetters.isNotEmpty()) {
                Text(
                    text = subLetters,
                    color = Color(0xFFAEC0D6),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    style = TextStyle(shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), offset = Offset(0f, -0.5f)))
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Recents Tab — REAL call log
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecentsTabView(
    recents: List<RealCallEntry>,
    hasPermission: Boolean,
    onCallContact: (String) -> Unit,
    onRequestPermission: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        IOS4NavigationBar(title = "Recents")

        if (!hasPermission) {
            PermissionPromptView(
                message = "需要通话记录权限才能显示真实记录",
                onRequest = onRequestPermission
            )
            return@Column
        }

        if (recents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("无通话记录", color = Color(0xFF8E8E93), fontSize = 16.sp, fontFamily = FontFamily.SansSerif)
            }
            return@Column
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(recents, key = { it.id }) { call ->
                RecentCallRow(call = call, onCallContact = onCallContact)
                Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
            }
        }
    }
}

@Composable
private fun RecentCallRow(call: RealCallEntry, onCallContact: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCallContact(call.number) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (call.isMissed) Color(0xFFD32F2F) else Color.Black,
                fontFamily = FontFamily.SansSerif,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (call.name != call.number && call.number.isNotBlank()) {
                Text(
                    text = call.number,
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = call.timeDisplay, fontSize = 13.sp, color = Color(0xFF265696), fontFamily = FontFamily.SansSerif)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF45B411), modifier = Modifier.size(18.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Favorites Tab — show first 20 contacts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FavoritesTabView(favorites: List<RealContact>, onCallContact: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        IOS4NavigationBar(title = "Favorites")

        val display = favorites.take(20)

        if (display.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("无联系人", color = Color(0xFF8E8E93), fontSize = 16.sp, fontFamily = FontFamily.SansSerif)
            }
            return@Column
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(display, key = { it.contactId + it.number }) { contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCallContact(contact.number) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = contact.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily.SansSerif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "${contact.numberType}  ${contact.number}", fontSize = 13.sp, color = Color(0xFF757575), fontFamily = FontFamily.SansSerif)
                    }
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Favorite", tint = Color(0xFF1E64D8), modifier = Modifier.size(20.dp))
                }
                Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Contacts Tab — REAL contacts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ContactsTabView(
    contacts: List<RealContact>,
    hasPermission: Boolean,
    onCallContact: (String) -> Unit,
    onRequestPermission: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        IOS4NavigationBar(title = "All Contacts")

        if (!hasPermission) {
            PermissionPromptView(message = "需要联系人权限才能显示通讯录", onRequest = onRequestPermission)
            return@Column
        }

        if (contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("无联系人", color = Color(0xFF8E8E93), fontSize = 16.sp, fontFamily = FontFamily.SansSerif)
            }
            return@Column
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(contacts, key = { it.contactId + it.number }) { c ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCallContact(c.number) }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = c.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily.SansSerif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = c.number, fontSize = 13.sp, color = Color(0xFF757575), fontFamily = FontFamily.SansSerif)
                    }
                    Icon(imageVector = Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF45B411), modifier = Modifier.size(18.dp))
                }
                Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Voicemail Tab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VoicemailTabView(onCallVoicemail: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        IOS4NavigationBar(title = "Voicemail")

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_tab_voicemail),
                contentDescription = "Voicemail",
                colorFilter = ColorFilter.tint(Color(0xFF8E8E93)),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Call Voicemail", fontSize = 17.sp, color = Color(0xFF265696), fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tap to call your voicemail inbox", fontSize = 13.sp, color = Color(0xFF8E8E93), fontFamily = FontFamily.SansSerif)
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(colors = listOf(Color(0xFF7DE640), Color(0xFF56C91D), Color(0xFF389D0C)))
                    )
                    .clickable { onCallVoicemail() }
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Call Now", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Permission prompt helper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PermissionPromptView(message: String, onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = Color(0xFF8E8E93), fontSize = 15.sp, fontFamily = FontFamily.SansSerif, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(colors = listOf(Color(0xFF5BB8FF), Color(0xFF2D81D0))))
                .clickable { onRequest() }
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Text("授予权限", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// iOS 4 Tab Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IOS4PhoneTabBar(currentTab: PhoneTab, onTabSelected: (PhoneTab) -> Unit) {
    val tabs = listOf(
        PhoneTab.FAVORITES  to Pair(R.drawable.ic_tab_favorites, "Favorites"),
        PhoneTab.RECENTS    to Pair(R.drawable.ic_tab_recents,   "Recents"),
        PhoneTab.CONTACTS   to Pair(R.drawable.ic_tab_contacts,  "Contacts"),
        PhoneTab.KEYPAD     to Pair(R.drawable.ic_tab_keypad,    "Keypad"),
        PhoneTab.VOICEMAIL  to Pair(R.drawable.ic_tab_voicemail, "Voicemail"),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(49.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3D3D3D), Color(0xFF1A1A1A), Color(0xFF0A0A0A))
                )
            )
            .border(width = 0.5.dp, color = Color(0xFF5A5A5A))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            for ((tab, iconPair) in tabs) {
                val isSelected = currentTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onTabSelected(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = iconPair.first),
                            contentDescription = iconPair.second,
                            modifier = Modifier.size(24.dp),
                            colorFilter = if (!isSelected)
                                ColorFilter.tint(Color(0xFF8E8E93))
                            else null
                        )
                        Text(
                            text = iconPair.second,
                            color = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF8E8E93),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
