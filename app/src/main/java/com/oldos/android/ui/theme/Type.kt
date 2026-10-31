package com.oldos.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.oldos.android.R

/**
 * Inter 4.1 (SIL Open Font License 1.1) stands in for Helvetica Neue, which
 * cannot be legally redistributed (see licenses/Inter-OFL.txt). Latin glyphs
 * render in Inter; CJK and other scripts missing from Inter fall through to
 * the platform font chain automatically.
 */
val IOS4FontFamily = FontFamily(
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

/**
 * iOS 4 type scale, canonical 320pt values
 * (ref: nos4 packages/UIKit/src per-component UI metrics files,
 * packages/SpringBoard/src/Support/SpringBoardMetrics.ts).
 * All sizes are in design-dp on the 320pt canvas — see [IOS4DesignCanvas].
 */
// Status bar: 12pt bold time / 12pt medium carrier, aux 11pt
val IOS4StatusBarTimeStyle = TextStyle(
    fontFamily = IOS4FontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 12.sp
)

val IOS4StatusBarTextStyle = TextStyle(
    fontFamily = IOS4FontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp
)

val IOS4StatusBarAuxStyle = TextStyle(
    fontFamily = IOS4FontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp
)

// Navigation bar: 18pt bold title, ~12pt bold button
val IOS4NavTitleStyle = TextStyle(
    fontFamily = IOS4FontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp
)

val IOS4NavButtonStyle = TextStyle(
    fontFamily = IOS4FontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 12.sp
)

// Springboard icon label: 11pt medium with heavy drop shadow
val IOS4IconLabelStyle = TextStyle(
    fontFamily = IOS4FontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp
)

// Grouped table rows: 17pt regular, detail 15pt, section header 14pt
val IOS4TableRowStyle = TextStyle(
    fontFamily = IOS4FontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 17.sp
)

val IOS4TableRowDetailStyle = TextStyle(
    fontFamily = IOS4FontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp
)

val IOS4SectionHeaderStyle = TextStyle(
    fontFamily = IOS4FontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp
)

// Tab bar labels: 10pt
val IOS4TabLabelStyle = TextStyle(
    fontFamily = IOS4FontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp
)

// Lock screen: thin clock ~56pt, date 16pt, slide-to-unlock 23pt
val IOS4LockClockStyle = TextStyle(
    fontFamily = IOS4FontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 56.sp
)

val IOS4LockDateStyle = TextStyle(
    fontFamily = IOS4FontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp
)

val IOS4SlideToUnlockStyle = TextStyle(
    fontFamily = IOS4FontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 23.sp
)

val OldOSTypography = Typography(
    displayLarge = IOS4LockClockStyle,
    displayMedium = IOS4LockClockStyle.copy(fontSize = 48.sp),
    headlineMedium = IOS4NavTitleStyle,
    titleLarge = IOS4NavTitleStyle,
    titleMedium = TextStyle(
        fontFamily = IOS4FontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp
    ),
    bodyLarge = IOS4TableRowStyle,
    bodyMedium = TextStyle(
        fontFamily = IOS4FontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    bodySmall = IOS4TabLabelStyle,
    labelLarge = IOS4NavButtonStyle,
    labelMedium = IOS4IconLabelStyle,
    labelSmall = IOS4TabLabelStyle
)
