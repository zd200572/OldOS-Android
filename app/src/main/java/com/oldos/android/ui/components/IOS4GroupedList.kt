package com.oldos.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.ui.theme.IOS4GroupRowDivider
import com.oldos.android.ui.theme.IOS4GroupStroke
import com.oldos.android.ui.theme.IOS4PinstripeA
import com.oldos.android.ui.theme.IOS4PinstripeB
import com.oldos.android.ui.theme.IOS4RowDetailText
import com.oldos.android.ui.theme.IOS4RowPressedGradient
import com.oldos.android.ui.theme.IOS4RowValueText
import com.oldos.android.ui.theme.IOS4SectionHeaderStyle
import com.oldos.android.ui.theme.IOS4SectionHeaderText
import com.oldos.android.ui.theme.IOS4TableRowDetailStyle
import com.oldos.android.ui.theme.IOS4TableRowStyle
import kotlin.math.roundToInt

/**
 * The iOS 4 "pinstripe" grouped-table background: vertical stripes,
 * 12pt pitch — rgb(203,210,216) 0-1.5px, rgb(197,204,212) 1.5-10.5px,
 * rgb(203,210,216) 10.5-12px.
 * (nos4: packages/UIKit/src/Table/UITableMetrics.ts)
 * Rendered once into a tiny ImageBitmap and tiled with a ShaderBrush.
 */
@Composable
fun rememberIOS4PinstripeBrush(): ShaderBrush {
    val density = LocalDensity.current
    return remember(density) {
        val pitch = with(density) { 12.dp.toPx() }
        val stripeA = with(density) { 1.5.dp.toPx() }
        val stripeBStart = stripeA
        val stripeBEnd = pitch - stripeA

        val width = pitch.roundToInt().coerceAtLeast(4)
        val bitmap = android.graphics.Bitmap.createBitmap(
            width, 1, android.graphics.Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint()

        paint.color = IOS4PinstripeA.toArgb()
        canvas.drawRect(0f, 0f, stripeBStart, 1f, paint)
        paint.color = IOS4PinstripeB.toArgb()
        canvas.drawRect(stripeBStart, 0f, stripeBEnd, 1f, paint)
        paint.color = IOS4PinstripeA.toArgb()
        canvas.drawRect(stripeBEnd, 0f, width.toFloat(), 1f, paint)

        ShaderBrush(ImageShader(bitmap.asImageBitmap(), TileMode.Repeated, TileMode.Repeated))
    }
}

/** Paints the pinstripe texture behind a screen (Settings, Notes list, etc.). */
fun Modifier.ios4PinstripeBackground(brush: ShaderBrush): Modifier = drawBehind {
    drawRect(brush)
}

@Composable
fun IOS4GroupCard(
    modifier: Modifier = Modifier,
    headerTitle: String? = null,
    footerText: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        if (headerTitle != null) {
            Text(
                text = headerTitle,
                color = IOS4SectionHeaderText,
                style = IOS4SectionHeaderStyle,
                modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(width = 0.5.dp, color = IOS4GroupStroke, shape = RoundedCornerShape(10.dp)),
            content = content
        )

        if (footerText != null) {
            Text(
                text = footerText,
                color = IOS4SectionHeaderText,
                style = IOS4SectionHeaderStyle.copy(fontSize = 13.sp),
                modifier = Modifier.padding(start = 12.dp, top = 6.dp)
            )
        }
    }
}

@Composable
fun IOS4RowItem(
    title: String,
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    detailText: String? = null,
    showChevron: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .then(
                if (isPressed) {
                    Modifier.background(Brush.verticalGradient(IOS4RowPressedGradient))
                } else Modifier
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onClick() }
                } else Modifier
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val titleColor = if (isPressed) Color.White else Color.Black

        if (iconRes != null) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Text(
            text = title,
            color = titleColor,
            style = IOS4TableRowStyle,
            modifier = Modifier.weight(1f)
        )

        if (detailText != null) {
            Text(
                text = detailText,
                color = if (isPressed) Color.White else IOS4RowValueText,
                style = IOS4TableRowDetailStyle,
                modifier = Modifier.padding(end = if (showChevron) 6.dp else 0.dp)
            )
        }

        if (trailingContent != null) {
            trailingContent()
        } else if (showChevron) {
            IOS4ChevronGlyph(tint = if (isPressed) Color.White else IOS4RowDetailText)
        }
    }
}

/** Slender iOS chevron, replaces the Material glyph. */
@Composable
fun IOS4ChevronGlyph(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = 9.dp, height = 16.dp)) {
        val stroke = 2.dp.toPx()
        val midX = size.width / 2f
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(stroke / 2f, stroke * 0.9f)
            lineTo(midX + size.width * 0.28f, size.height / 2f)
            lineTo(stroke / 2f, size.height - stroke * 0.9f)
        }
        drawPath(
            path,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = stroke,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@Composable
fun IOS4RowDivider(
    hasIcon: Boolean = true
) {
    HorizontalDivider(
        modifier = Modifier.padding(start = if (hasIcon) 52.dp else 12.dp),
        thickness = 0.5.dp,
        color = IOS4GroupRowDivider
    )
}
