package com.oldos.android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.oldos.android.model.AppItem
import com.oldos.android.ui.components.IOS4NavigationBar
import com.oldos.android.ui.components.IOS4StatusBar

@Composable
fun AppPlaceholderScreen(
    app: AppItem,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFC5CCD4)) // Classic iOS 4 striped gray-blue background
    ) {
        IOS4StatusBar()
        IOS4NavigationBar(
            title = app.title,
            leftButtonText = "Home",
            onLeftButtonClick = onHomeClick
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = app.iconRes),
                    contentDescription = app.title,
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                )

                Text(
                    text = "${app.title} (iOS 4)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    fontFamily = FontFamily.SansSerif
                )

                Text(
                    text = "Module in development for OldOS Android.",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}
