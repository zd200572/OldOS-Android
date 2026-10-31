package com.oldos.android.ui.screens

import android.content.ContentValues
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.ui.components.IOS4StatusBar
import com.oldos.android.ui.components.IOS4StatusBarVariant
import com.oldos.android.ui.theme.IOS4FontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * iOS 4 Camera: shutter bar with film-roll slot. The shutter hands off to the
 * system camera via ACTION_IMAGE_CAPTURE; the shot lands in MediaStore so it
 * shows up in the Photos camera roll.
 */
@Composable
fun CameraScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var lastShot by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var flash by remember { mutableStateOf(false) }

    val shutterLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingUri
        if (success && uri != null) {
            scope.launch {
                lastShot = withContext(Dispatchers.IO) {
                    context.contentResolver.loadThumbnail(uri, android.util.Size(320, 320), null)
                }
            }
        }
        flash = false
    }

    fun shoot() {
        flash = true
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "OldOS_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )
        pendingUri = uri
        if (uri != null) shutterLauncher.launch(uri) else flash = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Overlay)

        // Viewfinder
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0A0A0A))
        ) {
            // Viewfinder grid
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stepX = size.width / 3f
                val stepY = size.height / 3f
                for (i in 1..2) {
                    drawLine(
                        Color.White.copy(alpha = 0.12f),
                        Offset(stepX * i, 0f), Offset(stepX * i, size.height), strokeWidth = 1f
                    )
                    drawLine(
                        Color.White.copy(alpha = 0.12f),
                        Offset(0f, stepY * i), Offset(size.width, stepY * i), strokeWidth = 1f
                    )
                }
            }

            if (flash) {
                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.85f)))
            }

            lastShot?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Last photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.7f)
                )
                Text(
                    text = "Saved to Camera Roll",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontFamily = IOS4FontFamily,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 18.dp)
                )
            }
        }

        // Shutter bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2A2A2C), Color(0xFF101012))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Shutter button
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1C))
                    .border(2.5.dp, Color(0xFFDDDDDE), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { shoot() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(34.dp)) {
                    drawArc(
                        color = Color(0xFFB9B9BE),
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(size.width * 0.1f, size.height * 0.1f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.8f, size.height * 0.8f),
                        style = Stroke(width = 3.4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }
            }

            // Home hint (bottom center below bar handled by chin)
            Text(
                text = "Camera",
                color = Color(0xFF8E8E93),
                fontSize = 11.sp,
                fontFamily = IOS4FontFamily,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
            )
        }
    }
}
