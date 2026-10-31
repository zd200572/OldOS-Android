package com.oldos.android.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.oldos.android.model.PhotoItem
import com.oldos.android.model.PhotosRepository
import com.oldos.android.ui.components.*
import com.oldos.android.ui.theme.IOS4FontFamily

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * iOS 4 Photos: 4-column camera roll grid with thin gaps, tap for a
 * full-screen swipeable viewer. Reads the real device gallery via MediaStore.
 */
@Composable
fun PhotosScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var photos by remember { mutableStateOf<List<PhotoItem>>(emptyList()) }
    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    var hasPermission by remember { mutableStateOf(hasGalleryPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        // Full grant or Android 14 partial ("Select photos") both count
        hasPermission = grants.values.any { it }
        if (hasPermission) {
            photos = PhotosRepository.loadPhotos(context)
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission && photos.isEmpty()) {
            photos = withContext(Dispatchers.IO) { PhotosRepository.loadPhotos(context) }
        }
    }

    if (viewerIndex != null) {
        PhotoViewer(
            photos = photos,
            initialIndex = viewerIndex!!,
            onClose = { viewerIndex = null }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = "Camera Roll",
            leftButtonText = "Home",
            onLeftButtonClick = onHomeClick
        )

        if (!hasPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Photos access is required to show your camera roll.",
                        color = Color(0xFF9C9CA1),
                        fontSize = 14.sp,
                        fontFamily = IOS4FontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    com.oldos.android.ui.theme.IOS4NavButtonBlueGradient
                                )
                            )
                            .clickable { permissionLauncher.launch(galleryPermissions()) }
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
        } else if (photos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No Photos",
                    color = Color(0xFF6B6B70),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = IOS4FontFamily
                )
            }
        } else {
            // iOS 4 camera roll: 4 columns, hairline gaps
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(photos) { photo ->
                    val index = photos.indexOf(photo)
                    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, photo) {
                        value = withContext(Dispatchers.IO) {
                            PhotosRepository.thumbnail(context, photo, 256)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(Color(0xFF1A1A1A))
                            .clickable { viewerIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoViewer(
    photos: List<PhotoItem>,
    initialIndex: Int,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { photos.size }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Overlay)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color(0xCC000000)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${pagerState.currentPage + 1} of ${photos.size}",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = IOS4FontFamily
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(5.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            com.oldos.android.ui.theme.IOS4NavButtonBlueGradient
                        )
                    )
                    .clickable { onClose() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Done",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = IOS4FontFamily
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { page ->
            val photo = photos.getOrNull(page) ?: return@HorizontalPager
            val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, photo) {
                value = withContext(Dispatchers.IO) {
                    try {
                        val resolver = context.contentResolver
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            resolver.loadThumbnail(photo.uri, android.util.Size(2048, 2048), null)
                        } else {
                            MediaStoreCompat.decode(resolver, photo.uri)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Date footer, iOS 4 style
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCC000000))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = photos.getOrNull(pagerState.currentPage)
                    ?.let { PhotosRepository.formatDate(it.dateTakenMs) } ?: "",
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = IOS4FontFamily
            )
        }
    }
}

private fun hasGalleryPermission(context: android.content.Context): Boolean {
    return galleryPermissions().any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

private fun galleryPermissions(): Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
} else {
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

private object MediaStoreCompat {
    fun decode(resolver: android.content.ContentResolver, uri: android.net.Uri): android.graphics.Bitmap? {
        return android.provider.MediaStore.Images.Media.getBitmap(resolver, uri)
    }
}
