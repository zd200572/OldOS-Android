package com.oldos.android.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.oldos.android.ui.components.IOS4NavigationBar
import com.oldos.android.ui.components.IOS4StatusBar
import com.oldos.android.ui.components.IOS4StatusBarVariant
import com.oldos.android.ui.theme.IOS4FontFamily

/**
 * A WebView app with iOS 4 chrome: used for YouTube and Maps — sites that
 * genuinely work as mobile web apps inside the retro shell.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebAppScreen(
    title: String,
    startUrl: String,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = title,
            leftButtonText = "Home",
            onLeftButtonClick = onHomeClick
        )

        // Thin blue progress line
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((progress / 100f).coerceIn(0.02f, 1f))
                    .height(2.dp)
                    .background(Color(0xFF3B78E7))
            )
        }

        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                        }
                    }
                    loadUrl(startUrl)
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}
