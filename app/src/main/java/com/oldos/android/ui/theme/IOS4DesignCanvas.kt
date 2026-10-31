package com.oldos.android.ui.theme

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import kotlin.math.roundToInt

/**
 * iOS 4 was designed on a 320x480pt canvas. Modern Android phones are much
 * wider (360-430dp) and far taller (20:9), so instead of letterboxing we map
 * one design point onto screenWidth/320 real pixels: the whole OS scales up
 * proportionally with the physical width and simply grows taller vertically
 * (more springboard rows, longer scrolling lists) — the same behavior the
 * original OldOS app exhibits on modern iPhones.
 *
 * Dynamic font scaling (system font size preference) is intentionally
 * disabled: the OS replica layout breaks if text grows independently of the
 * surrounding chrome.
 */
const val IOS4_DESIGN_WIDTH = 320

@Composable
fun IOS4DesignCanvas(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val designDensity = remember(configuration, density) {
        Density(
            density = configuration.screenWidthDp * density.density / IOS4_DESIGN_WIDTH.toFloat(),
            fontScale = 1f
        )
    }

    val designConfiguration = remember(configuration) {
        Configuration(configuration).apply {
            val scale = IOS4_DESIGN_WIDTH.toFloat() / configuration.screenWidthDp
            screenWidthDp = IOS4_DESIGN_WIDTH
            screenHeightDp = (configuration.screenHeightDp * scale).roundToInt()
            fontScale = 1f
        }
    }

    CompositionLocalProvider(
        LocalDensity provides designDensity,
        LocalConfiguration provides designConfiguration
    ) {
        content()
    }
}
