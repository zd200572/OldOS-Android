package com.oldos.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.R
import com.oldos.android.model.AppItem
import com.oldos.android.ui.theme.IOS4BadgeBorder
import com.oldos.android.ui.theme.IOS4BadgeRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun IOS4AppIcon(
    app: AppItem,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    showLabel: Boolean = true,
    showShadow: Boolean = true,
    isEditMode: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    // Wiggle animation in edit mode
    val infiniteTransition = rememberInfiniteTransition(label = "icon_wiggle")
    val rotation by if (isEditMode) {
        infiniteTransition.animateFloat(
            initialValue = -2.5f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 110, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wiggle_rotation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "icon_press_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(82.dp)
            .rotate(rotation)
            .scale(scale)
            .pointerInput(isEditMode) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            // 1. Classic wallpaper floating shadow behind SpringBoard icons
            if (showShadow) {
                Image(
                    painter = painterResource(id = R.drawable.wallpaper_icon_shadow),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(size * 1.73f)
                        .offset(y = 4.dp)
                )
            }

            // 2. The Apple iOS 4 Retina Icon (Rendered faithfully without clipping)
            Image(
                painter = painterResource(id = app.iconRes),
                contentDescription = app.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(size)
            )

            // 3. Dynamic Calendar Text overlay for Calendar App
            if (app.id == "calendar") {
                val cal = remember { Calendar.getInstance() }
                val weekday = remember(cal) {
                    SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time).uppercase()
                }
                val day = remember(cal) {
                    cal.get(Calendar.DAY_OF_MONTH).toString()
                }

                Column(
                    modifier = Modifier
                        .size(size)
                        .padding(top = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Weekday name in red header bar
                    Text(
                        text = weekday,
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(0f, 1f),
                                blurRadius = 1f
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    // Date number in center white field
                    Text(
                        text = day,
                        color = Color(0xFF222222),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 32.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }
            }

            // 4. Subtle darken overlay on touch press
            if (isPressed) {
                Box(
                    modifier = Modifier
                        .size(size * 0.96f)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color.Black.copy(alpha = 0.28f))
                )
            }

            // 5. Notification Badge (Perfect circle for 1 digit, round horizontal pill for multiple digits)
            if (app.badgeCount > 0 && !isEditMode) {
                val badgeStr = if (app.badgeCount > 99) "99+" else "${app.badgeCount}"
                val isSingleDigit = badgeStr.length == 1

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-3).dp)
                        .then(
                            if (isSingleDigit) {
                                Modifier.size(20.dp)
                            } else {
                                Modifier
                                    .height(20.dp)
                                    .defaultMinSize(minWidth = 20.dp)
                                    .padding(horizontal = 5.dp)
                            }
                        )
                        .shadow(elevation = 2.dp, shape = CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    IOS4BadgeRed,
                                    Color(0xFF9E0B0B)
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(1.5.dp, IOS4BadgeBorder, CircleShape)
                ) {
                    Text(
                        text = badgeStr,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
            }

            // 6. Delete badge in Edit Mode (jiggle mode)
            if (isEditMode && !app.isSystemApp) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-6).dp, y = (-6).dp)
                        .size(22.dp)
                        .background(Color(0xFF424242), CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                        .clickable { onDeleteClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Remove",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // App title below icon
        if (showLabel) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = app.title,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.95f),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 2.5f
                    )
                )
            )
        }
    }
}
