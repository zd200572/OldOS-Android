package com.oldos.android.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oldos.android.ui.components.IOS4NavigationBar
import com.oldos.android.ui.components.IOS4StatusBar
import com.oldos.android.ui.components.IOS4NavigationBar
import com.oldos.android.ui.components.IOS4StatusBarVariant
import com.oldos.android.ui.theme.IOS4FontFamily

/**
 * Static, faithful-in-spirit recreations of the store/auxiliary iOS 4 apps
 * whose real backends no longer exist (or never existed on Android).
 */

@Composable
private fun DemoChrome(
    title: String,
    onHomeClick: () -> Unit,
    background: Brush,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = title,
            leftButtonText = "Home",
            onLeftButtonClick = onHomeClick
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            content = content
        )
    }
}

// ── iTunes ───────────────────────────────────────────────────────────────────

@Composable
fun ITunesScreen(onHomeClick: () -> Unit, modifier: Modifier = Modifier) {
    DemoChrome(
        title = "iTunes",
        onHomeClick = onHomeClick,
        background = Brush.verticalGradient(listOf(Color(0xFF2A2A5C), Color(0xFF14142E)))
    ) {
        // Featured banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF7C5CD6), Color(0xFF4A3A96))))
                .padding(vertical = 26.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "♪",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 42.sp
                )
                Text(
                    text = "Featured: The 2010 Essentials",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = IOS4FontFamily
                )
            }
        }

        StoreSectionHeader("Top Songs")
        StoreSongRow("1", "Hey, Soul Sister", "Train", "$1.29")
        StoreSongRow("2", "Airplanes", "B.o.B", "$1.29")
        StoreSongRow("3", "OMG", "Usher", "$1.29")

        StoreSectionHeader("Top Albums")
        StoreSongRow("1", "Recovery", "Eminem", "$11.99")
        StoreSongRow("2", "My Beautiful Dark Twisted Fantasy", "Kanye West", "$13.99")

        Text(
            text = "The iTunes Store of 2010 is retired. This storefront is a museum piece.",
            color = Color(0x66FFFFFF),
            fontSize = 11.sp,
            fontFamily = IOS4FontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
        )
    }
}

// ── App Store ────────────────────────────────────────────────────────────────

@Composable
fun AppStoreScreen(onHomeClick: () -> Unit, modifier: Modifier = Modifier) {
    DemoChrome(
        title = "App Store",
        onHomeClick = onHomeClick,
        background = Brush.verticalGradient(listOf(Color(0xFF3E4A5C), Color(0xFF12161E)))
    ) {
        StoreSectionHeader("New & Noteworthy")
        StoreAppRow("OldOS", "The whole era, in one app", "GET", Color(0xFF3B78E7))
        StoreAppRow("Doom", "Classic FPS, now on iOS 4", "GET", Color(0xFF3B78E7))
        StoreAppRow("Flatty Bird", "One tap. That's the game.", "GET", Color(0xFF3B78E7))

        StoreSectionHeader("What's Hot")
        StoreAppRow("FaceTime", "Video calls, WiFi only", "FREE", Color(0xFF3B78E7))
        StoreAppRow("Compass", "Know where north is", "FREE", Color(0xFF3B78E7))

        Text(
            text = "Serving the internet of 2010 since 2010. Inventory frozen.",
            color = Color(0x66FFFFFF),
            fontSize = 11.sp,
            fontFamily = IOS4FontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
        )
    }
}

// ── Game Center ──────────────────────────────────────────────────────────────

@Composable
fun GameCenterScreen(onHomeClick: () -> Unit, modifier: Modifier = Modifier) {
    DemoChrome(
        title = "Game Center",
        onHomeClick = onHomeClick,
        background = Brush.verticalGradient(listOf(Color(0xFF5B5B8A), Color(0xFF23233E)))
    ) {
        // Player card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x26000000))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF7DE640), Color(0xFF389D0C)))),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "ME", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = IOS4FontFamily)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "OldOS Player", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = IOS4FontFamily)
                Text(text = "0 friends · 0 games", color = Color(0xFFB9B9CE), fontSize = 12.sp, fontFamily = IOS4FontFamily)
            }
        }

        StoreSectionHeader("Your Games")
        StoreAppRow("Doom", "Leaderboards available", "OPEN", Color(0xFF7DE640))
        StoreAppRow("Flatty Bird", "High score: —", "OPEN", Color(0xFF7DE640))

        Text(
            text = "Matchmaking servers for iOS 4 have signed off for good.",
            color = Color(0x66FFFFFF),
            fontSize = 11.sp,
            fontFamily = IOS4FontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
        )
    }
}

// ── FaceTime ─────────────────────────────────────────────────────────────────

@Composable
fun FaceTimeScreen(onHomeClick: () -> Unit, modifier: Modifier = Modifier) {
    DemoChrome(
        title = "FaceTime",
        onHomeClick = onHomeClick,
        background = Brush.verticalGradient(listOf(Color(0xFF3A4652), Color(0xFF14181E)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x26000000))
                .padding(vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera lens
            Canvas(modifier = Modifier.size(84.dp)) {
                drawCircle(Color(0xFF101014), radius = size.minDimension / 2f)
                drawCircle(
                    Color(0xFF2E4A6E),
                    radius = size.minDimension * 0.32f,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(Color(0xFF3B78E7).copy(alpha = 0.6f), radius = size.minDimension * 0.14f)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "FaceTime",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = IOS4FontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Video calls over Wi-Fi between iPhone 4s.\nNo iPhone 4s were connected to this network —\nso FaceTime stays a beautiful memory.",
                color = Color(0xFFB9C2CC),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontFamily = IOS4FontFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Nike + iPod ──────────────────────────────────────────────────────────────

@Composable
fun NikeScreen(onHomeClick: () -> Unit, modifier: Modifier = Modifier) {
    DemoChrome(
        title = "Nike + iPod",
        onHomeClick = onHomeClick,
        background = Brush.verticalGradient(listOf(Color(0xFF2A2A2C), Color(0xFF0E0E0E)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x26000000))
                .padding(vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "0.00",
                color = Color(0xFF7DE640),
                fontSize = 58.sp,
                fontWeight = FontWeight.Thin,
                fontFamily = IOS4FontFamily
            )
            Text(
                text = "KILOMETERS TODAY",
                color = Color(0xFF9C9CA1),
                fontSize = 12.sp,
                fontFamily = IOS4FontFamily
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Pair your Nike+ sensor to start tracking.\n(The sensor, like 2010, is no longer manufactured.)",
                color = Color(0xFF9C9CA1),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontFamily = IOS4FontFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Cydia ────────────────────────────────────────────────────────────────────

@Composable
fun CydiaScreen(onHomeClick: () -> Unit, modifier: Modifier = Modifier) {
    DemoChrome(
        title = "Cydia",
        onHomeClick = onHomeClick,
        background = Brush.verticalGradient(listOf(Color(0xFF5A4A2E), Color(0xFF241C0F)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x26000000))
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📦",
                fontSize = 44.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Nothing to jailbreak.",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = IOS4FontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "OldOS already gives you the good parts:\nfolders, multitasking, wallpapers —\nand it never phones home to Cupertino.",
                color = Color(0xFFCDBFA0),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontFamily = IOS4FontFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Shared pieces ────────────────────────────────────────────────────────────

@Composable
private fun StoreSectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFFFF9500),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = IOS4FontFamily,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun StoreSongRow(index: String, title: String, subtitle: String, price: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index,
            color = Color(0xFF9AA7B8),
            fontSize = 15.sp,
            fontFamily = IOS4FontFamily,
            modifier = Modifier.width(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = IOS4FontFamily,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = Color(0xFF9AA7B8),
                fontSize = 12.sp,
                fontFamily = IOS4FontFamily,
                maxLines = 1
            )
        }
        Text(
            text = price,
            color = Color(0xFF9AA7B8),
            fontSize = 13.sp,
            fontFamily = IOS4FontFamily
        )
    }
}

@Composable
private fun StoreAppRow(title: String, subtitle: String, action: String, actionColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon placeholder tile
        Canvas(modifier = Modifier.size(44.dp)) {
            drawRoundRect(
                color = Color(0xFF5A6474),
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx())
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = IOS4FontFamily,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = Color(0xFF9AA7B8),
                fontSize = 12.sp,
                fontFamily = IOS4FontFamily,
                maxLines = 1
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(Brush.verticalGradient(listOf(actionColor, actionColor.copy(alpha = 0.75f))))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        ) {
            Text(
                text = action,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = IOS4FontFamily
            )
        }
    }
}
