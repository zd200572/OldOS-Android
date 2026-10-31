package com.oldos.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.oldos.android.ui.theme.IOS4PageDotActive
import com.oldos.android.ui.theme.IOS4PageDotIdle

/**
 * Springboard page dots. Page 0 (Spotlight) is drawn as a magnifier glyph,
 * faithful to iOS 4.
 * (nos4: packages/SpringBoard/src/Support/SpringBoardMetrics.ts —
 * dots 7.9 diameter, 9 spacing, active white / idle rgb(146,146,146))
 */
@Composable
fun IOS4PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Spotlight magnifier for page 0
        IOS4SpotlightGlyph(
            active = currentPage == 0,
            modifier = Modifier.padding(end = 3.dp)
        )

        // Normal page dots (pages 1..pageCount)
        for (page in 1..pageCount) {
            val isActive = (currentPage == page)
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(
                        color = if (isActive) IOS4PageDotActive else IOS4PageDotIdle,
                        shape = CircleShape
                    )
            )

            if (page < pageCount) {
                Spacer(modifier = Modifier.width(9.dp))
            }
        }
    }
}

@Composable
private fun IOS4SpotlightGlyph(
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (active) IOS4PageDotActive else IOS4PageDotIdle

    Canvas(modifier = modifier.size(width = 10.dp, height = 10.dp)) {
        val stroke = 1.1.dp.toPx()
        val lensRadius = size.minDimension * 0.30f
        val lensCenter = Offset(size.width * 0.42f, size.height * 0.42f)

        drawCircle(
            color = color,
            radius = lensRadius,
            center = lensCenter,
            style = Stroke(width = stroke)
        )
        // Handle
        drawLine(
            color = color,
            start = Offset(
                lensCenter.x + lensRadius * 0.72f,
                lensCenter.y + lensRadius * 0.72f
            ),
            end = Offset(size.width * 0.92f, size.height * 0.92f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}
