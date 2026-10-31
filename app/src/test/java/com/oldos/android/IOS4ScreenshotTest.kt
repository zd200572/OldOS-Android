package com.oldos.android

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.oldos.android.model.AppItem
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
    fun settings() {
        composeRule.setContent {
            OldOSTheme { SettingsScreen(onHomeClick = {}) }
        }
        capture("03_settings")
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
