package com.oldos.android.ui.screens

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.ui.components.*
import com.oldos.android.ui.theme.IOS4FontFamily

import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * iOS 4 Voice Memos: red record button, elapsed timer, and a tape-list of
 * recordings stored in the app's private directory, playable in place.
 */
@Composable
fun VoiceMemosScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val memosDir = remember { File(context.filesDir, "voice_memos").apply { mkdirs() } }

    var recordings by remember { mutableStateOf(memosDir.listFiles { f -> f.extension == "m4a" }?.sortedByDescending { it.lastModified() } ?: emptyList()) }
    var isRecording by remember { mutableStateOf(false) }
    var elapsedSec by remember { mutableIntStateOf(0) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentFile by remember { mutableStateOf<File?>(null) }
    var playingFile by remember { mutableStateOf<File?>(null) }
    val player = remember { TapePlayer() }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            elapsedSec++
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recorder?.release()
            player?.stop()
        }
    }

    fun startRecording() {
        val outFile = File(memosDir, "Memo ${SimpleDateFormat("MMM d, HH.mm", Locale.US).format(Date())}.m4a")
        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION") MediaRecorder()
        }
        try {
            rec.setAudioSource(MediaRecorder.AudioSource.MIC)
            rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            rec.setOutputFile(outFile.absolutePath)
            rec.prepare()
            rec.start()
            recorder = rec
            currentFile = outFile
            elapsedSec = 0
            isRecording = true
        } catch (e: Exception) {
            rec.release()
        }
    }

    fun stopRecording() {
        try {
            recorder?.stop()
            recordings = memosDir.listFiles { f -> f.extension == "m4a" }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } catch (_: Exception) {
            currentFile?.delete()
        } finally {
            recorder?.release()
            recorder = null
            isRecording = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .ios4PinstripeBackground(rememberIOS4PinstripeBrush())
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = "Voice Memos",
            leftButtonText = "Home",
            onLeftButtonClick = onHomeClick
        )

        // Recorder panel (dark tape-deck strip)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF2A2A2C), Color(0xFF0E0E0E))))
                .border(0.5.dp, Color(0xFFABABAB), RoundedCornerShape(10.dp))
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%02d:%02d", elapsedSec / 60, elapsedSec % 60),
                color = if (isRecording) Color(0xFFFF453A) else Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Thin,
                fontFamily = IOS4FontFamily
            )
            Spacer(modifier = Modifier.height(14.dp))
            // Record / Stop button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2C2E))
                    .border(1.5.dp, Color(0xFF5A5A5E), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (isRecording) stopRecording() else startRecording()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFF453A))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF453A))
                    )
                }
            }
            Text(
                text = if (isRecording) "Tap to Stop" else "Record",
                color = Color(0xFF9C9CA1),
                fontSize = 12.sp,
                fontFamily = IOS4FontFamily,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Tape list
        if (recordings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No Recordings",
                    color = Color(0xFF8F8F8F),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = IOS4FontFamily
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(recordings) { file ->
                    val isPlayingThis = playingFile == file
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Speaker triangle / stop square
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isPlayingThis) Color(0xFF4C86EB) else Color(0xFFE3E5E8))
                                .clickable {
                                    if (isPlayingThis) {
                                        player.stop()
                                        playingFile = null
                                    } else {
                                        player.play(file) { playingFile = null }
                                        playingFile = file
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPlayingThis) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color.White, RoundedCornerShape(2.dp))
                                )
                            } else {
                                Canvas(modifier = Modifier.size(14.dp)) {
                                    val path = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(size.width * 0.25f, 0f)
                                        lineTo(size.width, size.height / 2f)
                                        lineTo(size.width * 0.25f, size.height)
                                        close()
                                    }
                                    drawPath(path, color = Color(0xFF3E5383))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.nameWithoutExtension,
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontFamily = IOS4FontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = SimpleDateFormat("EEEE, MMMM d", Locale.US).format(Date(file.lastModified())),
                                color = Color(0xFF8F8F8F),
                                fontSize = 12.sp,
                                fontFamily = IOS4FontFamily
                            )
                        }
                        Text(
                            text = "Delete",
                            color = Color(0xFF3E5383),
                            fontSize = 14.sp,
                            fontFamily = IOS4FontFamily,
                            modifier = Modifier.clickable {
                                if (playingFile == file) {
                                    player?.stop()
                                    playingFile = null
                                }
                                file.delete()
                                recordings = memosDir.listFiles { f -> f.extension == "m4a" }?.sortedByDescending { it.lastModified() } ?: emptyList()
                            }
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

/** Simple MediaPlayer wrapper for playing back a memo. */
private class TapePlayer {
    var mp: MediaPlayer? = null

    fun play(file: File, onDone: () -> Unit) {
        stop()
        mp = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener { onDone() }
                start()
            } catch (e: Exception) {
                onDone()
            }
        }
    }

    fun stop() {
        mp?.release()
        mp = null
    }
}
