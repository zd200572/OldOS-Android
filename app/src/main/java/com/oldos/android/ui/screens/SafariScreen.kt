package com.oldos.android.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.oldos.android.ui.components.IOS4StatusBar
import com.oldos.android.ui.components.IOS4StatusBarVariant
import com.oldos.android.ui.theme.IOS4FontFamily
import com.oldos.android.ui.theme.IOS4NavButtonGradient
import com.oldos.android.ui.theme.IOS4NavBarEdge

private data class Bookmark(val title: String, val url: String)

private val safariBookmarks = listOf(
    Bookmark("Google", "https://www.google.com/webhp?num=30"),
    Bookmark("Wikipedia", "https://en.wikipedia.org/wiki/Portal:2000s"),
    Bookmark("Apple — 2010 (Archive)", "https://web.archive.org/web/2010/http://www.apple.com/"),
    Bookmark("YouTube", "https://www.youtube.com/"),
    Bookmark("Open-Meteo", "https://open-meteo.com/")
)

/**
 * iOS 4 Safari: WebView wrapped in the classic chrome — silver URL bar with
 * reload, thin progress line, and the five-button bottom toolbar.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SafariScreen(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var webUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(true) }

    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }

    fun load(bookmark: Bookmark) {
        showBookmarks = false
        webView?.loadUrl(bookmark.url)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF4A5768))
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)

        // URL bar (silver gradient + rounded field)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE8EAED), Color(0xFFC8CCD2), Color(0xFFADB3BC))
                    )
                )
                .padding(horizontal = 8.dp, vertical = 7.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF5B7BA6), Color(0xFF3E5E8C))))
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = webUrl.ifEmpty { "Bookmarks" },
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = IOS4FontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // Reload glyph
                Canvas(
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { webView?.reload() }
                ) {
                    val stroke = 1.8.dp.toPx()
                    drawArc(
                        color = Color.White,
                        startAngle = -60f,
                        sweepAngle = 300f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = StrokeCap.Round)
                    )
                    // Arrowhead
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.72f, size.height * 0.06f),
                        end = Offset(size.width * 0.80f, size.height * 0.28f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Thin blue progress line
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((progress / 100f).coerceIn(0.02f, 1f))
                    .height(2.dp)
                    .background(Color(0xFF3B78E7))
            )
        }

        // Content: bookmarks overlay or the web
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                webUrl = url ?: ""
                                isLoading = true
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                canGoBack = view?.canGoBack() ?: false
                                canGoForward = view?.canGoForward() ?: false
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                            }
                        }
                    }
                },
                update = { webView = it },
                modifier = Modifier.fillMaxSize()
            )

            if (showBookmarks) {
                BookmarksOverlay(onOpen = ::load)
            }
        }

        // Bottom toolbar: back / forward / bookmarks / pages / new page
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    Brush.verticalGradient(
                        IOS4NavButtonGradient
                    )
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarGlyph(kind = ToolbarIcon.BACK, enabled = canGoBack) {
                if (webView?.canGoBack() == true) webView?.goBack()
            }
            ToolbarGlyph(kind = ToolbarIcon.FORWARD, enabled = canGoForward) {
                if (webView?.canGoForward() == true) webView?.goForward()
            }
            ToolbarGlyph(kind = ToolbarIcon.BOOKS, enabled = true) {
                showBookmarks = !showBookmarks
            }
            ToolbarGlyph(kind = ToolbarIcon.PAGES, enabled = true) {
                showBookmarks = true
            }
            ToolbarGlyph(kind = ToolbarIcon.PLUS, enabled = true) {
                webView?.loadUrl("about:blank")
                showBookmarks = true
            }
        }
    }
}

@Composable
private fun BookmarksOverlay(onOpen: (Bookmark) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCFD5DB))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFB4BFCD), Color(0xFF6E85A2))
                    )
                )
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Bookmarks",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = IOS4FontFamily
            )
        }

        Column {
            safariBookmarks.forEach { bookmark ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .clickable { onOpen(bookmark) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(22.dp)) {
                        // little book glyph
                        drawRoundRect(
                            color = Color(0xFF4C86EB),
                            topLeft = Offset(0f, size.height * 0.12f),
                            size = Size(size.width * 0.72f, size.height * 0.76f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                        drawRect(
                            color = Color(0xFF2E63C9),
                            topLeft = Offset(size.width * 0.28f, size.height * 0.18f),
                            size = Size(size.width * 0.66f, size.height * 0.64f)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = bookmark.title,
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontFamily = IOS4FontFamily
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(Color(0xFFABABAB))
                )
            }
        }

        Text(
            text = "Pages load from the live web. Sites from the 2010 era may look different today.",
            color = Color(0xFF6B7280),
            fontSize = 11.sp,
            fontFamily = IOS4FontFamily,
            lineHeight = 15.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

private enum class ToolbarIcon { BACK, FORWARD, BOOKS, PAGES, PLUS }

@Composable
private fun ToolbarGlyph(
    kind: ToolbarIcon,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val tint = if (enabled) Color.White else Color.White.copy(alpha = 0.4f)
    Canvas(
        modifier = Modifier
            .size(width = 56.dp, height = 30.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) { onClick() }
    ) {
        val w = size.width
        val h = size.height
        val stroke = 2.2.dp.toPx()
        val strokeStyle = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = StrokeCap.Round)

        when (kind) {
            ToolbarIcon.BACK -> {
                drawLine(tint, Offset(w * 0.62f, h * 0.24f), Offset(w * 0.38f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.38f, h * 0.5f), Offset(w * 0.62f, h * 0.76f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.38f, h * 0.5f), Offset(w * 0.82f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
            }
            ToolbarIcon.FORWARD -> {
                drawLine(tint, Offset(w * 0.38f, h * 0.24f), Offset(w * 0.62f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.62f, h * 0.5f), Offset(w * 0.38f, h * 0.76f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.62f, h * 0.5f), Offset(w * 0.18f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
            }
            ToolbarIcon.BOOKS -> {
                drawRoundRect(tint, topLeft = Offset(w * 0.28f, h * 0.15f), size = Size(w * 0.44f, h * 0.70f), cornerRadius = CornerRadius(2f, 2f), style = strokeStyle)
                drawLine(tint, Offset(w * 0.5f, h * 0.15f), Offset(w * 0.5f, h * 0.85f), strokeWidth = stroke * 0.7f)
            }
            ToolbarIcon.PAGES -> {
                drawRect(tint, topLeft = Offset(w * 0.42f, h * 0.14f), size = Size(w * 0.36f, h * 0.5f), style = strokeStyle)
                drawRect(tint, topLeft = Offset(w * 0.22f, h * 0.36f), size = Size(w * 0.36f, h * 0.5f), style = strokeStyle)
            }
            ToolbarIcon.PLUS -> {
                drawLine(tint, Offset(w * 0.5f, h * 0.2f), Offset(w * 0.5f, h * 0.8f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.2f, h * 0.5f), Offset(w * 0.8f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
            }
        }
    }
}
