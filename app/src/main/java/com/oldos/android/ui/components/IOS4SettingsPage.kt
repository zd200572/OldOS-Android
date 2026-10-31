package com.oldos.android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oldos.android.ui.components.IOS4StatusBarVariant

/**
 * Shared scaffolding for Settings sub-pages: pinstripe background, light
 * status bar, nav bar with the pointed back arrow, scrolling content column.
 */
@Composable
fun IOS4SettingsPage(
    title: String,
    backButtonText: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .ios4PinstripeBackground(rememberIOS4PinstripeBrush())
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = title,
            leftButtonText = backButtonText,
            onLeftButtonClick = onBack,
            isLeftBackButton = true
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
