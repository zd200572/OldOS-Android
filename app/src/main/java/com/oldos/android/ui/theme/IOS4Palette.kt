package com.oldos.android.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Central iOS 4 design tokens. Every value is either copied from the nos4
 * recreation's UIKit metrics (provenance noted per section, e.g.
 * "nos4: packages/UIKit/src/StatusBar/UIStatusBarMetrics.ts") or carried over
 * from the original OldOS assets. Values marked "approx" are best-effort
 * recreations not present in the reference sources.
 *
 * Colors only — dimensions live next to the components that use them.
 */

// ─────────────────────────────────────────────────────────────────────────────
// Status bar
// nos4: packages/UIKit/src/StatusBar/UIStatusBarMetrics.ts
// ─────────────────────────────────────────────────────────────────────────────

/** In-app light chrome: `linear-gradient(to bottom, rgb(237,244,247) 7%, rgb(191,199,203) 100%)` */
val IOS4StatusBarLightGradientTop = Color(0xFFEDF4F7)
val IOS4StatusBarLightGradientBottom = Color(0xFFBFC7CB)

/** In-app bottom edge line rgb(93,100,105) + inner shadow rgba(142,149,154,0.75) */
val IOS4StatusBarLightEdge = Color(0xFF5D6469)
val IOS4StatusBarLightInnerShadow = Color(0xBF8E959A)

/** In-app text: time rgb(0,0,0), carrier rgb(66,66,66), glyphs rgb(74,74,74) */
val IOS4StatusBarLightTime = Color(0xFF000000)
val IOS4StatusBarLightCarrier = Color(0xFF424242)
val IOS4StatusBarLightGlyph = Color(0xFF4A4A4A)
val IOS4StatusBarLightTextShadow = Color(0x80FFFFFF)

/** Overlay (dark) chrome: background rgba(0,0,0,0.65), text rgb(200,200,200) */
val IOS4StatusBarDarkBackground = Color(0xA6000000)
val IOS4StatusBarDarkText = Color(0xFFC8C8C8)

/** Wi-Fi glyph gradient rgb(32,157,237) → rgb(72,118,196) */
val IOS4WifiGradientTop = Color(0xFF209DED)
val IOS4WifiGradientBottom = Color(0xFF4876C4)

/** Battery fill gradient stops 0% / 24.3% / 48.7% / 73% */
val IOS4BatteryFillGradient = listOf(
    Color(0xFF6BD037),
    Color(0xFFD7FCB4),
    Color(0xFF86E249),
    Color(0xFF44A31D)
)
val IOS4BatteryLow = Color(0xFFFF3B30)

// ─────────────────────────────────────────────────────────────────────────────
// Navigation bar
// nos4: packages/UIKit/src/NavigationBar/UINavigationBarMetrics.ts
// ─────────────────────────────────────────────────────────────────────────────

/** Classic blue-gray bar: rgb(180,191,205) → rgb(136,155,179)@49% → rgb(128,149,175) → rgb(110,133,162) */
val IOS4NavBarGradient = listOf(
    Color(0xFFB4BFCD),
    Color(0xFF889BB3),
    Color(0xFF8095AF),
    Color(0xFF6E85A2)
)
val IOS4NavBarEdge = Color(0xFF2D3033)

/** Nav button (gray chrome): rgb(164,175,191) → rgb(124,141,164)@51% → rgb(113,131,156) → rgb(112,130,155) */
val IOS4NavButtonGradient = listOf(
    Color(0xFFA4AFBF),
    Color(0xFF7C8DA4),
    Color(0xFF71839C),
    Color(0xFF70829B)
)

/** Nav button (tinted blue-gray, back arrow): rgb(142,166,196) → rgb(88,119,166)@50% → rgb(71,105,153)@53.3% → rgb(74,108,155) */
val IOS4NavButtonTintedGradient = listOf(
    Color(0xFF8EA6C4),
    Color(0xFF5877A6),
    Color(0xFF476999),
    Color(0xFF4A6C9B)
)

/** Nav button (blue, done/send): rgb(137,173,238) → rgb(80,140,231)@51% → rgb(43,120,228)@52% → rgb(46,123,229) */
val IOS4NavButtonBlueGradient = listOf(
    Color(0xFF89ADEE),
    Color(0xFF508CE7),
    Color(0xFF2B78E4),
    Color(0xFF2E7BE5)
)

/** Nav button (destructive red): rgb(239,135,142) → rgb(199,52,63)@48% → rgb(189,20,33)@49% */
val IOS4NavButtonRedGradient = listOf(
    Color(0xFFEF878E),
    Color(0xFFC7343F),
    Color(0xFFBD1421)
)

/** Pressed darkening multiplier for chrome gradients (approx) */
val IOS4ChromePressedOverlay = Color(0x33000000)

// ─────────────────────────────────────────────────────────────────────────────
// Tab bar
// nos4: packages/UIKit/src/TabBar/UITabBarMetrics.ts
// ─────────────────────────────────────────────────────────────────────────────

val IOS4TabLabelIdle = Color(0xFFA8A8A8)
val IOS4TabLabelSelected = Color.White

/** Idle icon gradient rgb(157,157,157) → rgb(89,89,89) */
val IOS4TabIconIdleGradient = listOf(Color(0xFF9D9D9D), Color(0xFF595959))

/** Selected icon "blue glow": rgb(197,210,229) → rgb(99,162,216)@47% → rgb(0,145,230)@49% → rgb(21,197,252) */
val IOS4TabIconSelectedGradient = listOf(
    Color(0xFFC5D2E5),
    Color(0xFF63A2D8),
    Color(0xFF0091E6),
    Color(0xFF15C5FC)
)

/** Selected icon rim rgb(205,233,249) → rgb(75,220,251) */
val IOS4TabIconRimGradient = listOf(Color(0xFFCDE9F9), Color(0xFF4BDCFB))

/** Dark opaque tab bar (Phone app) — carried over from original implementation */
val IOS4TabBarDarkGradient = listOf(
    Color(0xFF3D3D3D),
    Color(0xFF1A1A1A),
    Color(0xFF0A0A0A)
)

// ─────────────────────────────────────────────────────────────────────────────
// Grouped table ("pinstripe" lists)
// nos4: packages/UIKit/src/Table/UITableMetrics.ts
// ─────────────────────────────────────────────────────────────────────────────

/** Pinstripe stripe pair rgb(203,210,216) / rgb(197,204,212), 12pt pitch */
val IOS4PinstripeA = Color(0xFFCBD2D8)
val IOS4PinstripeB = Color(0xFFC5CCD4)

/** Group container stroke rgb(171,171,171), radius 10, row height 44 */
val IOS4GroupStroke = Color(0xFFABABAB)
val IOS4GroupRowDivider = Color(0xFFE2E2E2)

/** Section header text rgb(76,86,108) */
val IOS4SectionHeaderText = Color(0xFF4C566C)

/** Row link/value rgb(62,83,131), secondary detail rgb(143,143,143) */
val IOS4RowValueText = Color(0xFF3E5383)
val IOS4RowDetailText = Color(0xFF8F8F8F)

/** Row pressed state — iOS 4 selection blue (approx) */
val IOS4RowPressedGradient = listOf(Color(0xFF4C86EB), Color(0xFF2E63C9))

// ─────────────────────────────────────────────────────────────────────────────
// Switch
// nos4: packages/UIKit/src/Switch/UISwitchMetrics.ts
// ─────────────────────────────────────────────────────────────────────────────

/** ON: rgb(46,110,183) → rgb(60,152,222)@12% → rgb(102,190,243)@50% → rgb(70,163,231)@52% → rgb(120,205,247) */
val IOS4SwitchOnGradient = listOf(
    Color(0xFF2E6EB7),
    Color(0xFF3C98DE),
    Color(0xFF66BEF3),
    Color(0xFF46A3E7),
    Color(0xFF78CDF7)
)

/** ON (orange, airplane mode): rgb(255,140,14) → rgb(253,177,72) */
val IOS4SwitchOnOrangeGradient = listOf(Color(0xFFFF8C0E), Color(0xFFFDB148))

/** OFF: rgb(226,226,226) → rgb(255,255,255)@55% → rgb(250,250,250) */
val IOS4SwitchOffGradient = listOf(
    Color(0xFFE2E2E2),
    Color(0xFFFFFFFF),
    Color(0xFFFAFAFA)
)

/** Knob: white → rgb(247,247,247)@48% → rgb(226,226,226)@52% → rgb(240,240,240), stroke rgb(140,140,140) */
val IOS4SwitchThumbGradient = listOf(
    Color(0xFFFFFFFF),
    Color(0xFFF7F7F7),
    Color(0xFFE2E2E2),
    Color(0xFFF0F0F0)
)
val IOS4SwitchThumbStroke = Color(0xFF8C8C8C)

// ─────────────────────────────────────────────────────────────────────────────
// Alert view
// nos4: packages/UIKit/src/Alert/UIAlertMetrics.ts
// ─────────────────────────────────────────────────────────────────────────────

/** Translucent navy body rgba(11,27,68,0.85) */
val IOS4AlertBody = Color(0xD90B1B44)
val IOS4AlertRimTop = Color(0xFFE2E3E4)
val IOS4AlertRimBottom = Color(0xFFB2B7C2)

// ─────────────────────────────────────────────────────────────────────────────
// SpringBoard
// nos4: packages/SpringBoard/src/Support/SpringBoardMetrics.ts
// ─────────────────────────────────────────────────────────────────────────────

val IOS4IconLabelText = Color.White
/** Label shadow rgba(0,0,0,0.9) */
val IOS4IconLabelShadow = Color(0xE6000000)

/** Page dots: active white, idle rgb(146,146,146) */
val IOS4PageDotActive = Color.White
val IOS4PageDotIdle = Color(0xFF929292)

/** Badge (nos4 does not specify; carried over) */
val IOS4BadgeRed = Color(0xFFE52929)
val IOS4BadgeRedDark = Color(0xFF9E0B0B)
val IOS4BadgeBorder = Color.White

// ─────────────────────────────────────────────────────────────────────────────
// Messages
// Carried over from the original implementation (OldOS-derived values)
// ─────────────────────────────────────────────────────────────────────────────

val IOS4BubbleSentGradient = listOf(
    Color(0xFF7DE640),
    Color(0xFF5AC61F),
    Color(0xFF45B411)
)
val IOS4BubbleSentBorder = Color(0xFF389D0C)

val IOS4BubbleReceivedGradient = listOf(
    Color(0xFFFFFFFF),
    Color(0xFFEBEBEB),
    Color(0xFFD6D6D6)
)
val IOS4BubbleReceivedBorder = Color(0xFFB8B8B8)

val IOS4MessagesBackground = Color(0xFFDCE2E8)

val IOS4InputBarGradient = listOf(
    Color(0xFFE4E7EC),
    Color(0xFFCBD0D8),
    Color(0xFFB5BAC3)
)

val IOS4SendButtonGradient = listOf(
    Color(0xFF65A5F2),
    Color(0xFF2E7DE5),
    Color(0xFF1F60B8)
)

/** Unread indicator / accent blue */
val IOS4AccentBlue = Color(0xFF2E62DE)

// ─────────────────────────────────────────────────────────────────────────────
// Phone
// Carried over from the original implementation
// ─────────────────────────────────────────────────────────────────────────────

val IOS4PhoneBackgroundGradient = listOf(
    Color(0xFF263C57),
    Color(0xFF14243A),
    Color(0xFF0A1523)
)

val IOS4PhoneKeypadGradient = listOf(
    Color(0xFF6E8199),
    Color(0xFF4C6584),
    Color(0xFF334F70)
)

val IOS4PhoneLcdGradient = listOf(
    Color(0xFF5B718D),
    Color(0xFF2A4261),
    Color(0xFF142742),
    Color(0xFF091628)
)

// ─────────────────────────────────────────────────────────────────────────────
// Calculator
// Carried over from the original implementation
// ─────────────────────────────────────────────────────────────────────────────

val IOS4CalcOperatorGradient = listOf(Color(0xFFF7941D), Color(0xFFD47008))
val IOS4CalcFunctionGradient = listOf(Color(0xFFA5A5A5), Color(0xFF7E7E7E))
val IOS4CalcDigitGradient = listOf(Color(0xFF4C4C4C), Color(0xFF2C2C2C))
val IOS4CalcLcdBackground = Color(0xFF1C1C1E)

// ─────────────────────────────────────────────────────────────────────────────
// Lock screen
// nos4: packages/SpringBoard lock screen metrics
// ─────────────────────────────────────────────────────────────────────────────

val IOS4LockHeaderBand = Color(0xB3000000)
val IOS4LockHeaderBandBottom = Color(0x8C000000)
val IOS4LockFooterScrim = listOf(
    Color(0x99292828),
    Color(0xB3151414),
    Color(0xD9000000)
)
val IOS4LockSliderGrooveGradient = listOf(
    Color(0xFF030303),
    Color(0xFF151515),
    Color(0xFF222222)
)
val IOS4LockSliderThumbGradient = listOf(
    Color(0xFFF6F6F6),
    Color(0xFFDFDFDF),
    Color(0xFFBCBCBC),
    Color(0xFF9E9E9E)
)
val IOS4LockSliderThumbGloss = listOf(
    Color(0xFFF3F3F3),
    Color(0xFFE1E1E1)
)
val IOS4LockBottomScrim = Color(0xFF222222)

// ─────────────────────────────────────────────────────────────────────────────
// Hardware chin (approx — no reference values)
// ─────────────────────────────────────────────────────────────────────────────

val IOS4ChinGradient = listOf(
    Color(0xFF000000),
    Color(0xFF161616),
    Color(0xFF000000)
)
val IOS4ChinTopHairline = Color(0xFF2E2E2E)
