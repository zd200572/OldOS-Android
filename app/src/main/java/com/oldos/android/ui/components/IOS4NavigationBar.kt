package com.oldos.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.ui.theme.IOS4NavBarGradientBottom
import com.oldos.android.ui.theme.IOS4NavBarGradientTop

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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        IOS4NavBarGradientTop,
                        Color(0xFF889BB3),
                        IOS4NavBarGradientBottom,
                        Color(0xFF4C6585)
                    )
                )
            )
            .border(width = 0.5.dp, color = Color(0xFF2C3E50))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Title with classic embossed shadow
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                color = Color.Black.copy(alpha = 0.6f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-1).dp)
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(30.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(5.dp))
            .clip(RoundedCornerShape(5.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF7A93B2),
                        Color(0xFF4F6E94),
                        Color(0xFF385376)
                    )
                )
            )
            .border(
                width = 0.5.dp,
                color = Color(0xFF23364D),
                shape = RoundedCornerShape(5.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF7A93B2),
                        Color(0xFF4F6E94),
                        Color(0xFF385376)
                    )
                )
            )
            .border(
                width = 0.8.dp,
                color = Color(0xFF23364D),
                shape = arrowShape
            )
            .clickable { onClick() }
            .padding(start = 14.dp, end = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )
    }
}
