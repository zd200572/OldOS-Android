package com.oldos.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                color = Color(0xFF4C566C),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(width = 0.5.dp, color = Color(0xFFABABAB), shape = RoundedCornerShape(10.dp)),
            content = content
        )

        if (footerText != null) {
            Text(
                text = footerText,
                color = Color(0xFF4C566C),
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Text(
            text = title,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.weight(1f)
        )

        if (detailText != null) {
            Text(
                text = detailText,
                color = Color(0xFF385487),
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(end = if (showChevron) 6.dp else 0.dp)
            )
        }

        if (trailingContent != null) {
            trailingContent()
        } else if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color(0xFF8E8E93),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun IOS4RowDivider(
    hasIcon: Boolean = true
) {
    HorizontalDivider(
        modifier = Modifier.padding(start = if (hasIcon) 52.dp else 12.dp),
        thickness = 0.5.dp,
        color = Color(0xFFE2E2E2)
    )
}
