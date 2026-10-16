package com.oldos.android.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
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
import com.oldos.android.data.RealSmsMessage
import com.oldos.android.data.RealSmsThread
import com.oldos.android.data.SystemDataRepository
import com.oldos.android.ui.components.IOS4NavigationBar
import com.oldos.android.ui.components.IOS4StatusBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
// Internal model used to add user-sent messages to real threads
// ─────────────────────────────────────────────────────────────────────────────

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromMe: Boolean,
    val timestamp: String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
)

// Adapter so the UI can work uniformly with both real SMS and user-sent messages
private fun RealSmsMessage.toChatMessage() = ChatMessage(
    id = id,
    text = body,
    isFromMe = isFromMe,
    timestamp = timestamp
)

// ─────────────────────────────────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MessagesScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun triggerVibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            else @Suppress("DEPRECATION") vibrator?.vibrate(durationMs)
        } catch (_: Exception) {}
    }

    // ── Real SMS state ────────────────────────────────────────────────────────
    val threadStates = remember { mutableStateListOf<ThreadState>() }
    var hasSmsPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun loadSmsData() {
        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            hasSmsPerm = true
            coroutineScope.launch {
                val threads = withContext(Dispatchers.IO) { SystemDataRepository.readSmsThreads(context) }
                threadStates.clear()
                threads.forEach { thread ->
                    threadStates += ThreadState(
                        thread = thread,
                        chatMessages = thread.messages.map { it.toChatMessage() }.toMutableList()
                    )
                }
            }
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.READ_SMS] == true) {
            hasSmsPerm = true
            loadSmsData()
        }
    }

    LaunchedEffect(Unit) {
        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            hasSmsPerm = true
            loadSmsData()
        } else {
            permLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS))
        }
    }

    // ── UI state ──────────────────────────────────────────────────────────────
    var selectedThreadState by remember { mutableStateOf<ThreadState?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    BackHandler(enabled = selectedThreadState != null) { selectedThreadState = null }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFDCE2E8))) {
        AnimatedContent(
            targetState = selectedThreadState,
            transitionSpec = {
                if (targetState != null)
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it / 3 } + fadeOut())
                else
                    (slideInHorizontally { -it / 3 } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
            },
            label = "messages_nav_transition"
        ) { ts ->
            if (ts == null) {
                // Conversation list
                ConversationListView(
                    threadStates = threadStates,
                    hasPermission = hasSmsPerm,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    isEditMode = isEditMode,
                    onToggleEditMode = { isEditMode = !isEditMode },
                    onDeleteThread = { state -> threadStates.remove(state) },
                    onSelectThread = { state ->
                        state.thread.isUnread = false
                        selectedThreadState = state
                    },
                    onComposeNew = { selectedThreadState = threadStates.firstOrNull() },
                    onRequestPermission = {
                        permLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS))
                    }
                )
            } else {
                // Chat view
                ChatConversationView(
                    threadState = ts,
                    onBackClick = { selectedThreadState = null },
                    onSendMessage = { text ->
                        ts.chatMessages.add(ChatMessage(text = text, isFromMe = true))
                        triggerVibrate(24)
                        // Note: we do NOT actually send an SMS — that requires SEND_SMS permission
                        // and user consent. Sending is a separate feature.
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Conversation List
// ─────────────────────────────────────────────────────────────────────────────

private data class ThreadState(val thread: RealSmsThread, val chatMessages: MutableList<ChatMessage>)

@Composable
private fun ConversationListView(
    threadStates: List<ThreadState>,
    hasPermission: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isEditMode: Boolean,
    onToggleEditMode: () -> Unit,
    onDeleteThread: (ThreadState) -> Unit,
    onSelectThread: (ThreadState) -> Unit,
    onComposeNew: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        IOS4StatusBar()

        IOS4NavigationBar(
            title = "Messages",
            leftButtonText = if (isEditMode) "Done" else "Edit",
            onLeftButtonClick = onToggleEditMode,
            rightContent = {
                Box(
                    modifier = Modifier
                        .height(30.dp).width(36.dp)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(5.dp))
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.verticalGradient(colors = listOf(Color(0xFF7A93B2), Color(0xFF4F6E94), Color(0xFF385376)))
                        )
                        .border(width = 0.5.dp, color = Color(0xFF23364D), shape = RoundedCornerShape(5.dp))
                        .clickable { onComposeNew() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_compose), contentDescription = "Compose", modifier = Modifier.size(17.dp))
                }
            }
        )

        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth().height(44.dp)
                .background(Brush.verticalGradient(colors = listOf(Color(0xFFE4E7EC), Color(0xFFCBD0D8), Color(0xFFB5BAC3))))
                .border(width = 0.5.dp, color = Color(0xFF9FA5AE))
                .padding(horizontal = 8.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color.White)
                    .border(width = 0.5.dp, color = Color(0xFFA5ABB4), shape = RoundedCornerShape(15.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF8E8E93), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) Text("Search", color = Color(0xFF8E8E93), fontSize = 14.sp, fontFamily = FontFamily.SansSerif)
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = Color.Black, fontFamily = FontFamily.SansSerif),
                            cursorBrush = SolidColor(Color(0xFF2E7DE5)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(16.dp).clickable { onSearchQueryChange("") })
                    }
                }
            }
        }

        // Permission prompt
        if (!hasPermission) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("需要短信读取权限才能显示真实对话", color = Color(0xFF8E8E93), fontSize = 15.sp, fontFamily = FontFamily.SansSerif, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.verticalGradient(colors = listOf(Color(0xFF5BB8FF), Color(0xFF2D81D0))))
                        .clickable { onRequestPermission() }
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text("授予权限", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                }
            }
            return@Column
        }

        val filtered = threadStates.filter {
            searchQuery.isEmpty() ||
                    it.thread.contactName.contains(searchQuery, ignoreCase = true) ||
                    it.thread.phoneNumber.contains(searchQuery) ||
                    it.chatMessages.any { m -> m.text.contains(searchQuery, ignoreCase = true) }
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("无短信记录", color = Color(0xFF8E8E93), fontSize = 16.sp, fontFamily = FontFamily.SansSerif)
            }
            return@Column
        }

        LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White)) {
            items(filtered, key = { it.thread.threadId }) { state ->
                val lastMsg = state.chatMessages.lastOrNull()?.text ?: state.thread.lastBody

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectThread(state) }
                            .padding(vertical = 11.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isEditMode) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 10.dp).size(22.dp)
                                    .background(Color(0xFFE53935), CircleShape)
                                    .clickable { onDeleteThread(state) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        } else if (state.thread.isUnread) {
                            Box(modifier = Modifier.padding(end = 8.dp).size(10.dp).background(Color(0xFF1B64D8), CircleShape))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.thread.contactName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontFamily = FontFamily.SansSerif,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = state.thread.dateDisplay,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF265696),
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lastMsg,
                                fontSize = 14.sp,
                                color = Color(0xFF737373),
                                fontFamily = FontFamily.SansSerif,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 17.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(20.dp).padding(start = 4.dp)
                        )
                    }
                    Divider(color = Color(0xFFE5E5E5), thickness = 0.5.dp, modifier = Modifier.padding(start = 14.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chat Conversation View
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChatConversationView(
    threadState: ThreadState,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(threadState.chatMessages.size) {
        if (threadState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(threadState.chatMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFDCE2E8))) {
        IOS4StatusBar()

        IOS4NavigationBar(
            title = threadState.thread.contactName,
            leftButtonText = "Messages",
            isLeftBackButton = true,
            onLeftButtonClick = onBackClick,
            rightButtonText = "Call",
            onRightButtonClick = { /* call action */ }
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFDCE2E8))) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date stamp header
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFC7CBD1).copy(alpha = 0.85f))
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = threadState.thread.dateDisplay,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4C525B),
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                }

                items(threadState.chatMessages, key = { it.id }) { msg ->
                    ChatBubbleItem(message = msg)
                }
            }
        }

        // Bottom input bar
        Box(
            modifier = Modifier
                .fillMaxWidth().height(48.dp)
                .background(Brush.verticalGradient(colors = listOf(Color(0xFFE2E5EA), Color(0xFFC7CBD1), Color(0xFFAEB4BD))))
                .border(width = 0.5.dp, color = Color(0xFF90969F))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Brush.verticalGradient(colors = listOf(Color(0xFF7A93B2), Color(0xFF4F6E94), Color(0xFF385376))))
                        .border(width = 0.5.dp, color = Color(0xFF23364D), shape = RoundedCornerShape(5.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.White, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f).height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(Color.White)
                        .border(width = 1.dp, color = Color(0xFFB5BAC1), shape = RoundedCornerShape(17.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputText.isEmpty()) Text("Text Message", color = Color(0xFF8E8E93), fontSize = 14.sp, fontFamily = FontFamily.SansSerif)
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black, fontFamily = FontFamily.SansSerif),
                        cursorBrush = SolidColor(Color(0xFF2E7DE5)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                val hasText = inputText.isNotBlank()
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .shadow(elevation = if (hasText) 2.dp else 0.dp, shape = RoundedCornerShape(5.dp))
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            if (hasText)
                                Brush.verticalGradient(colors = listOf(Color(0xFF65A5F2), Color(0xFF2E7DE5), Color(0xFF1F60B8)))
                            else
                                Brush.verticalGradient(colors = listOf(Color(0xFFC7CBD1), Color(0xFFB2B8C0)))
                        )
                        .border(
                            width = 0.5.dp,
                            color = if (hasText) Color(0xFF1A4D96) else Color(0xFF9EA4AC),
                            shape = RoundedCornerShape(5.dp)
                        )
                        .clickable(enabled = hasText) {
                            val t = inputText.trim()
                            if (t.isNotEmpty()) { onSendMessage(t); inputText = "" }
                        }
                        .padding(horizontal = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Send",
                        color = if (hasText) Color.White else Color.White.copy(alpha = 0.55f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        style = TextStyle(shadow = if (hasText) Shadow(color = Color.Black.copy(alpha = 0.4f), offset = Offset(0f, -1f)) else null)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chat Bubble — authentic iOS 4 green (sent) / silver (received)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChatBubbleItem(message: ChatMessage) {
    val isFromMe = message.isFromMe

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        val bubbleShape = remember(isFromMe) {
            GenericShape { size, _ ->
                val r = 16f
                if (isFromMe) {
                    moveTo(r, 0f)
                    lineTo(size.width - r, 0f)
                    quadraticBezierTo(size.width, 0f, size.width, r)
                    lineTo(size.width, size.height - 8f)
                    quadraticBezierTo(size.width + 4f, size.height, size.width + 8f, size.height)
                    quadraticBezierTo(size.width, size.height, size.width - 12f, size.height)
                    lineTo(r, size.height)
                    quadraticBezierTo(0f, size.height, 0f, size.height - r)
                    lineTo(0f, r)
                    quadraticBezierTo(0f, 0f, r, 0f)
                    close()
                } else {
                    moveTo(r, 0f)
                    lineTo(size.width - r, 0f)
                    quadraticBezierTo(size.width, 0f, size.width, r)
                    lineTo(size.width, size.height - r)
                    quadraticBezierTo(size.width, size.height, size.width - r, size.height)
                    lineTo(12f, size.height)
                    quadraticBezierTo(0f, size.height, -8f, size.height)
                    quadraticBezierTo(-4f, size.height, 0f, size.height - 8f)
                    lineTo(0f, r)
                    quadraticBezierTo(0f, 0f, r, 0f)
                    close()
                }
            }
        }

        Box(
            modifier = Modifier
                .widthIn(min = 60.dp, max = 270.dp)
                .shadow(elevation = 1.5.dp, shape = bubbleShape)
                .clip(bubbleShape)
                .background(
                    brush = if (isFromMe)
                        Brush.verticalGradient(colors = listOf(Color(0xFF7DE640), Color(0xFF5AC61F), Color(0xFF45B411)))
                    else
                        Brush.verticalGradient(colors = listOf(Color(0xFFFFFFFF), Color(0xFFEBEBEB), Color(0xFFD6D6D6)))
                )
                .border(
                    width = 0.5.dp,
                    color = if (isFromMe) Color(0xFF389D0C) else Color(0xFFB0B0B0),
                    shape = bubbleShape
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(18.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = if (isFromMe) 0.35f else 0.55f), Color.Transparent)
                        )
                    )
            )
            Text(
                text = message.text,
                color = if (isFromMe) Color.White else Color(0xFF1C1C1E),
                fontSize = 15.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                lineHeight = 19.sp,
                style = TextStyle(
                    shadow = if (isFromMe) Shadow(color = Color.Black.copy(alpha = 0.35f), offset = Offset(0f, -0.5f)) else null
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
            )
        }
    }
}
