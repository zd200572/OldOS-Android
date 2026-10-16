package com.oldos.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.oldos.android.R
import com.oldos.android.model.AppItem

@Composable
fun IOS4Dock(
    dockApps: List<AppItem>,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    onAppClick: (AppItem) -> Unit = {},
    onAppLongClick: (AppItem) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. 3D Glass dock shelf background image
        Image(
            painter = painterResource(id = R.drawable.sb_dock_bg),
            contentDescription = "Dock Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .align(Alignment.BottomCenter)
        )

        // 2. Dock icons + reflection container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .padding(bottom = 10.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            dockApps.take(4).forEach { app ->
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.width(82.dp)
                ) {
                    // Authentic subtle reflection on the 3D glass shelf
                    Box(
                        modifier = Modifier
                            .offset(y = 54.dp)
                            .height(22.dp)
                            .clipToBounds()
                            .scale(scaleX = 1f, scaleY = -0.55f)
                            .alpha(0.24f)
                    ) {
                        IOS4AppIcon(
                            app = app,
                            size = 62.dp,
                            showLabel = false,
                            showShadow = false,
                            isEditMode = false
                        )
                    }

                    // Main dock icon
                    IOS4AppIcon(
                        app = app,
                        size = 62.dp,
                        showLabel = false,
                        showShadow = false,
                        isEditMode = isEditMode,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) }
                    )
                }
            }
        }
    }
}
