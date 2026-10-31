package com.oldos.android.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.oldos.android.R
import com.oldos.android.model.AudioRepository
import com.oldos.android.model.MusicPlayerManager
import com.oldos.android.model.SongItem
import com.oldos.android.ui.components.*
import com.oldos.android.ui.theme.IOS4FontFamily

import com.oldos.android.ui.theme.IOS4TabBarDarkGradient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private enum class IPodTab(val label: String) { SONGS("Songs"), ARTISTS("Artists"), ALBUMS("Albums") }

/**
 * iOS 4 iPod: Songs / Artists / Albums over the real device library, with a
 * full-screen Now Playing view. Playback state is shared with the
 * multitasking tray through MusicPlayerManager.
 */
@Composable
fun IPodScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var songs by remember { mutableStateOf<List<SongItem>>(emptyList()) }
    var tab by remember { mutableStateOf(IPodTab.SONGS) }
    var nowPlaying by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(hasAudioPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasPermission = grants.values.any { it }
        if (hasPermission) {
            songs = AudioRepository.loadSongs(context)
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission && songs.isEmpty()) {
            songs = withContext(Dispatchers.IO) { AudioRepository.loadSongs(context) }
        }
    }

    // Track position while playing
    LaunchedEffect(MusicPlayerManager.isPlaying) {
        while (MusicPlayerManager.isPlaying) {
            MusicPlayerManager.updatePosition()
            delay(500)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                nowPlaying && MusicPlayerManager.currentSong != null -> {
                    NowPlayingView(onBack = { nowPlaying = false })
                }
                !hasPermission -> AudioPermissionGate(
                    onGrant = { permissionLauncher.launch(audioPermissions()) }
                )
                songs.isEmpty() -> EmptyLibrary()
                else -> when (tab) {
                    IPodTab.SONGS -> SongList(
                        songs = songs,
                        onPlay = { index ->
                            MusicPlayerManager.play(context, songs, index)
                            nowPlaying = true
                        }
                    )
                    IPodTab.ARTISTS -> GroupedList(
                        title = "Artists",
                        groups = songs.groupBy { it.artist }.toSortedMap(),
                        emptyLabel = "Artist",
                        onPlayGroup = { groupSongs, index ->
                            MusicPlayerManager.play(context, groupSongs, index)
                            nowPlaying = true
                        }
                    )
                    IPodTab.ALBUMS -> GroupedList(
                        title = "Albums",
                        groups = songs.groupBy { it.album }.toSortedMap(),
                        emptyLabel = "Album",
                        onPlayGroup = { groupSongs, index ->
                            MusicPlayerManager.play(context, groupSongs, index)
                            nowPlaying = true
                        }
                    )
                }
            }
        }

        IPodTabBar(current = tab, onSelect = { tab = it })
    }
}

@Composable
private fun AudioPermissionGate(onGrant: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Music access is required to show your library.",
                color = Color(0xFF9C9CA1),
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
                    .clickable { onGrant() }
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
}

@Composable
private fun EmptyLibrary() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No Music",
            color = Color(0xFF6B6B70),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = IOS4FontFamily
        )
    }
}

@Composable
private fun SongList(songs: List<SongItem>, onPlay: (Int) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(songs) { song ->
            val index = songs.indexOf(song)
            LibraryRow(
                primary = song.title,
                secondary = "${song.artist} — ${AudioRepository.formatDuration(song.durationMs)}",
                onClick = { onPlay(index) }
            )
        }
    }
}

@Composable
private fun GroupedList(
    title: String,
    groups: Map<String, List<SongItem>>,
    emptyLabel: String,
    onPlayGroup: (List<SongItem>, Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        groups.forEach { (name, groupSongs) ->
            item(key = "header_$name") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1C1E))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = name,
                        color = Color(0xFFFF9500),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = IOS4FontFamily
                    )
                }
            }
            items(groupSongs, key = { "group_${name}_${it.id}" }) { song ->
                val index = groupSongs.indexOf(song)
                LibraryRow(
                    primary = song.title,
                    secondary = AudioRepository.formatDuration(song.durationMs),
                    onClick = { onPlayGroup(groupSongs, index) }
                )
            }
        }
    }
}

@Composable
private fun LibraryRow(
    primary: String,
    secondary: String,
    onClick: () -> Unit
) {
    val isCurrent = MusicPlayerManager.currentSong?.title == primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0E0E0E))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = primary,
                color = if (isCurrent) Color(0xFF4BA3F5) else Color.White,
                fontSize = 16.sp,
                fontFamily = IOS4FontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = secondary,
                color = Color(0xFF9C9CA1),
                fontSize = 12.sp,
                fontFamily = IOS4FontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IOS4ChevronGlyph(tint = Color(0xFF6B6B70))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Color(0xFF2A2A2C))
    )
}

@Composable
private fun NowPlayingView(onBack: () -> Unit) {
    val context = LocalContext.current
    val song = MusicPlayerManager.currentSong ?: return

    val albumArt by produceState<android.graphics.Bitmap?>(initialValue = null, song.id) {
        value = withContext(Dispatchers.IO) { AudioRepository.albumArt(context, song) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF2A2A2C), Color(0xFF0A0A0A)))
            )
    ) {
        IOS4NavigationBar(
            title = "Now Playing",
            leftButtonText = "iPod",
            isLeftBackButton = true,
            onLeftButtonClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // Album art (or placeholder)
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1C1C1E)),
                contentAlignment = Alignment.Center
            ) {
                albumArt?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Album art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Text(
                    text = "♪",
                    color = Color(0xFF6B6B70),
                    fontSize = 64.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = song.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = IOS4FontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artist} — ${song.album}",
                color = Color(0xFF9C9CA1),
                fontSize = 14.sp,
                fontFamily = IOS4FontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress + times
            val position = MusicPlayerManager.positionMs.toFloat() / 1000f
            val total = song.durationMs.toFloat().coerceAtLeast(1f) / 1000f
            IOS4Slider(
                value = (position / total).coerceIn(0f, 1f),
                onValueChange = { MusicPlayerManager.seekTo(it) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = AudioRepository.formatDuration(MusicPlayerManager.positionMs),
                    color = Color(0xFF9C9CA1),
                    fontSize = 10.sp,
                    fontFamily = IOS4FontFamily
                )
                Text(
                    text = "-" + AudioRepository.formatDuration((song.durationMs - MusicPlayerManager.positionMs).coerceAtLeast(0)),
                    color = Color(0xFF9C9CA1),
                    fontSize = 10.sp,
                    fontFamily = IOS4FontFamily
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Transport controls with the original PNG glyphs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.mc_prev),
                    contentDescription = "Previous",
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { MusicPlayerManager.previous(context) }
                )
                Spacer(modifier = Modifier.width(24.dp))
                Image(
                    painter = androidx.compose.ui.res.painterResource(
                        id = if (MusicPlayerManager.isPlaying) R.drawable.mc_pause else R.drawable.mc_play
                    ),
                    contentDescription = "Play/Pause",
                    modifier = Modifier
                        .size(58.dp)
                        .clickable { MusicPlayerManager.togglePlayPause(context) }
                )
                Spacer(modifier = Modifier.width(24.dp))
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.mc_next),
                    contentDescription = "Next",
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { MusicPlayerManager.next(context) }
                )
            }
        }
    }
}

@Composable
private fun IPodTabBar(
    current: IPodTab,
    onSelect: (IPodTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(IOS4TabBarDarkGradient))
            .padding(vertical = 6.dp)
    ) {
        IPodTab.entries.forEach { tabEntry ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(tabEntry) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MusicGlyph(kind = tabEntry, selected = current == tabEntry)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tabEntry.label,
                    color = if (current == tabEntry) Color.White else Color(0xFFA8A8A8),
                    fontSize = 10.sp,
                    fontFamily = IOS4FontFamily,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MusicGlyph(kind: IPodTab, selected: Boolean) {
    val tint = if (selected) Color.White else Color(0xFFA8A8A8)
    Canvas(modifier = Modifier.size(width = 22.dp, height = 18.dp)) {
        val stroke = 1.5.dp.toPx()
        val strokeStyle = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
        when (kind) {
            IPodTab.SONGS -> {
                // Eighth note
                drawCircle(tint, radius = size.width * 0.14f, center = Offset(size.width * 0.32f, size.height * 0.78f), style = strokeStyle)
                drawLine(tint, Offset(size.width * 0.46f, size.height * 0.76f), Offset(size.width * 0.46f, size.height * 0.15f), strokeWidth = stroke * 0.9f)
                drawLine(tint, Offset(size.width * 0.46f, size.height * 0.15f), Offset(size.width * 0.78f, size.height * 0.24f), strokeWidth = stroke * 0.9f)
            }
            IPodTab.ARTISTS -> {
                // Person
                drawCircle(tint, radius = size.width * 0.16f, center = Offset(size.width / 2f, size.height * 0.28f), style = strokeStyle)
                drawArc(
                    tint,
                    startAngle = 180f, sweepAngle = 180f, useCenter = false,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.48f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.7f),
                    style = strokeStyle
                )
            }
            IPodTab.ALBUMS -> {
                // Two stacked squares
                drawRect(tint, topLeft = Offset(size.width * 0.28f, size.height * 0.08f), size = androidx.compose.ui.geometry.Size(size.width * 0.6f, size.height * 0.6f), style = strokeStyle)
                drawRect(tint, topLeft = Offset(size.width * 0.14f, size.height * 0.3f), size = androidx.compose.ui.geometry.Size(size.width * 0.6f, size.height * 0.6f), style = strokeStyle)
            }
        }
    }
}

private fun hasAudioPermission(context: android.content.Context): Boolean {
    return audioPermissions().any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

private fun audioPermissions(): Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
} else {
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}
