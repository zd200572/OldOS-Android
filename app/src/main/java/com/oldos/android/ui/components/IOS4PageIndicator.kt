package com.oldos.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
        // Spotlight Search dot / icon
        val isSearchSelected = (currentPage == 0)
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = if (isSearchSelected) Color.White else Color.White.copy(alpha = 0.4f),
            modifier = Modifier
                .size(10.dp)
                .padding(end = 2.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Normal page dots
        for (page in 1..pageCount) {
            val isSelected = (currentPage == page)
            Box(
                modifier = Modifier
                    .size(6.5.dp)
                    .background(
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )

            if (page < pageCount) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}
