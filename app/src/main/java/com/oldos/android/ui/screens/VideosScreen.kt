package com.oldos.android.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.oldos.android.ui.components.*
import com.oldos.android.ui.theme.IOS4FontFamily

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class VideoItem(val id: Long, val uri: Uri, val displayName: String, val durationMs: Long)

/**
 * iOS 4 Videos: grid of the device's movies (MediaStore.Video) with a
 * full-screen player.
 */
@Composable
fun VideosScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var videos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var playing by remember { mutableStateOf<VideoItem?>(null) }
    var hasPermission by remember { mutableStateOf(hasVideoPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasPermission = grants.values.any { it }
        if (hasPermission) {
            videos = loadVideos(context)
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission && videos.isEmpty()) {
            videos = withContext(Dispatchers.IO) { loadVideos(context) }
        }
    }

    if (playing != null) {
        VideoPlayerScreen(video = playing!!, onClose = { playing = null })
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = "Videos",
            leftButtonText = "Home",
            onLeftButtonClick = onHomeClick
        )

        if (!hasPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Videos access is required to show your movies.",
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
                            .clickable { permissionLauncher.launch(videoPermissions()) }
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
        } else if (videos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No Videos",
                    color = Color(0xFF6B6B70),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = IOS4FontFamily
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(videos) { video ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1A1A1C))
                            .clickable { playing = video }
                    ) {
                        // Film-strip style tile
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .background(Color(0xFF2A2A2C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(34.dp)) {
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(size.width * 0.28f, 0f)
                                    lineTo(size.width, size.height / 2f)
                                    lineTo(size.width * 0.28f, size.height)
                                    close()
                                }
                                drawPath(path, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                        Text(
                            text = video.displayName.removeSuffix(".mp4").removeSuffix(".mov"),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = IOS4FontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoPlayerScreen(video: VideoItem, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Overlay)
        IOS4NavigationBar(
            title = video.displayName.removeSuffix(".mp4").removeSuffix(".mov"),
            leftButtonText = "Videos",
            isLeftBackButton = true,
            onLeftButtonClick = onClose
        )

        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(video.uri)
                    setMediaController(android.widget.MediaController(ctx))
                    start()
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

private fun hasVideoPermission(context: android.content.Context): Boolean =
    videoPermissions().any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

private fun videoPermissions(): Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
} else {
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

private fun loadVideos(context: android.content.Context): List<VideoItem> {
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }
    val videos = mutableListOf<VideoItem>()
    try {
        context.contentResolver.query(
            collection,
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION
            ),
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                videos += VideoItem(
                    id = cursor.getLong(0),
                    uri = android.content.ContentUris.withAppendedId(collection, cursor.getLong(0)),
                    displayName = cursor.getString(1) ?: "Video",
                    durationMs = cursor.getLong(2)
                )
            }
        }
    } catch (_: Exception) {
    }
    return videos
}
