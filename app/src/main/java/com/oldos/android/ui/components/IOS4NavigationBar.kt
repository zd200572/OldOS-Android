package com.oldos.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.oldos.android.ui.theme.IOS4ChromePressedOverlay
import com.oldos.android.ui.theme.IOS4FontFamily
import com.oldos.android.ui.theme.IOS4NavButtonGradient
import com.oldos.android.ui.theme.IOS4NavButtonRedGradient
import com.oldos.android.ui.theme.IOS4NavButtonStyle
import com.oldos.android.ui.theme.IOS4NavButtonTintedGradient
import com.oldos.android.ui.theme.IOS4NavBarEdge
import com.oldos.android.ui.theme.IOS4NavBarGradient
import com.oldos.android.ui.theme.IOS4NavTitleStyle

/**
 * Classic iOS 4 blue-gray navigation bar.
 * Gradient (nos4: packages/UIKit/src/NavigationBar/UINavigationBarMetrics.ts):
 * rgb(180,191,205) → rgb(136,155,179)@49% → rgb(128,149,175) → rgb(110,133,162).
 */
@Composable
fun IOS4NavigationBar(
    title: String,
    modifier: Modifier = Modifier,
    leftButtonText: String? = null,
    onLeftButtonClick: () -> Unit = {},
    isLeftBackButton: Boolean = false,
    rightButtonText: String? = null,
    onRightButtonClick: () -> Unit = {},
    leftContent: (@Composable () -> Unit)? = null,
    rightContent: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .drawBehind {
                val w = size.width
                val h = size.height
                // Hard "lip" between stops 2 and 3, faithful to the metal edge
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to IOS4NavBarGradient[0],
                            0.49f to IOS4NavBarGradient[1],
                            0.495f to IOS4NavBarGradient[2],
                            1f to IOS4NavBarGradient[3]
                        )
                    )
                )
                drawRect(
                    color = IOS4NavBarEdge,
                    topLeft = Offset(0f, h - 0.5f),
                    size = Size(w, 0.5f)
                )
            }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Title with classic embossed shadow
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                color = Color.Black.copy(alpha = 0.6f),
                style = IOS4NavTitleStyle,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.offset(y = (-1).dp)
            )
            Text(
                text = title,
                color = Color.White,
                style = IOS4NavTitleStyle,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // Left button / content
        Box(modifier = Modifier.align(Alignment.CenterStart)) {
            if (leftContent != null) {
                leftContent()
            } else if (leftButtonText != null) {
                if (isLeftBackButton) {
                    IOS4NavBackButton(
                        text = leftButtonText,
                        onClick = onLeftButtonClick
                    )
                } else {
                    IOS4NavButton(
                        text = leftButtonText,
                        onClick = onLeftButtonClick
                    )
                }
            }
        }

        // Right button / content
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            if (rightContent != null) {
                rightContent()
            } else if (rightButtonText != null) {
                IOS4NavButton(
                    text = rightButtonText,
                    onClick = onRightButtonClick
                )
            }
        }
    }
}

@Composable
fun IOS4NavButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tinted: Boolean = false,
    destructive: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val gradient = when {
        destructive -> IOS4NavButtonRedGradient
        tinted -> IOS4NavButtonTintedGradient
        else -> IOS4NavButtonGradient
    }

    Box(
        modifier = modifier
            .height(30.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Brush.verticalGradient(gradient))
            .then(
                if (isPressed) {
                    Modifier.background(IOS4ChromePressedOverlay)
                } else Modifier
            )
            .border(
                width = 0.5.dp,
                color = IOS4NavBarEdge,
                shape = RoundedCornerShape(5.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = IOS4NavButtonStyle
        )
    }
}

/**
 * Authentic pointed arrow back button of iOS 4
 */
@Composable
fun IOS4NavBackButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val arrowShape = GenericShape { size, _ ->
        val arrowW = 12f
        moveTo(arrowW, 0f)
        lineTo(size.width - 5f, 0f)
        quadraticBezierTo(size.width, 0f, size.width, 5f)
        lineTo(size.width, size.height - 5f)
        quadraticBezierTo(size.width, size.height, size.width - 5f, size.height)
        lineTo(arrowW, size.height)
        lineTo(0f, size.height / 2f)
        close()
    }

    Box(
        modifier = modifier
            .height(30.dp)
            .clip(arrowShape)
            .background(Brush.verticalGradient(IOS4NavButtonTintedGradient))
            .then(
                if (isPressed) {
                    Modifier.background(IOS4ChromePressedOverlay)
                } else Modifier
            )
            .border(
                width = 0.8.dp,
                color = IOS4NavBarEdge,
                shape = arrowShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(start = 14.dp, end = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = IOS4NavButtonStyle
        )
    }
}
