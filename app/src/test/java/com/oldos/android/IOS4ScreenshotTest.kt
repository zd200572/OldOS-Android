package com.oldos.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.oldos.android.model.AppItem
import com.oldos.android.ui.components.IOS4PageIndicator
import com.oldos.android.ui.screens.AppPlaceholderScreen
import com.oldos.android.ui.screens.CalculatorScreen
import com.oldos.android.ui.screens.LockScreen
import com.oldos.android.ui.screens.PhoneScreen
import com.oldos.android.ui.screens.SettingsScreen
import com.oldos.android.ui.screens.SpringBoardScreen
import com.oldos.android.ui.theme.OldOSTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi screenshot suite: renders each screen headlessly (no device
 * needed) so the look can be compared against the nos4 reference between
 * changes. Record with `gradlew recordRoborazziDebug`, PNGs land in
 * app/build/roborazzi/.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class IOS4ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun capture(name: String) {
        composeRule.onRoot().captureRoboImage("build/roborazzi/$name.png")
    }

    @Test
    fun lockScreen() {
        composeRule.setContent {
            OldOSTheme { LockScreen(onUnlocked = {}) }
        }
        capture("01_lock")
    }

    @Test
    fun springboard() {
        composeRule.setContent {
            OldOSTheme {
                SpringBoardScreen(
                    onAppOpen = {},
                    isEditMode = false,
                    onEditModeChange = {}
                )
            }
        }
        capture("02_springboard")
    }

    @Test
    fun spotlight() {
        composeRule.setContent {
            OldOSTheme {
                SpringBoardScreen(
                    onAppOpen = {},
                    isEditMode = false,
                    onEditModeChange = {},
                    initialPage = 0
                )
            }
        }
        capture("02b_spotlight")
    }

    @Test
    fun pageIndicatorDebug() {
        composeRule.setContent {
            OldOSTheme {
                Box(
                    modifier = Modifier
                        .size(width = 200.dp, height = 40.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    IOS4PageIndicator(pageCount = 2, currentPage = 1)
                }
            }
        }
        capture("_indicator_debug")
    }

    @Test
    fun settings() {
        composeRule.setContent {
            OldOSTheme { SettingsScreen(onHomeClick = {}) }
        }
        capture("03_settings")
    }

    @Test
    fun soundsSettings() {
        composeRule.setContent {
            OldOSTheme { com.oldos.android.ui.screens.SoundsSettingsScreen(onBack = {}) }
        }
        capture("03b_sounds")
    }

    @Test
    fun brightnessSettings() {
        composeRule.setContent {
            OldOSTheme { com.oldos.android.ui.screens.BrightnessSettingsScreen(onBack = {}) }
        }
        capture("03c_brightness")
    }

    @Test
    fun calculator() {
        composeRule.setContent {
            OldOSTheme { CalculatorScreen(onHomeClick = {}) }
        }
        capture("04_calculator")
    }

    @Test
    fun phone() {
        composeRule.setContent {
            OldOSTheme { PhoneScreen(onHomeClick = {}) }
        }
        capture("05_phone")
    }

    @Test
    fun placeholder() {
        composeRule.setContent {
            OldOSTheme {
                AppPlaceholderScreen(
                    app = AppItem("weather", "Weather", com.oldos.android.R.drawable.ic_weather),
                    onHomeClick = {}
                )
            }
        }
        capture("06_placeholder")
    }
}
