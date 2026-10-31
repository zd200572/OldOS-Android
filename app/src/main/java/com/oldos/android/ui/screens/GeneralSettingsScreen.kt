package com.oldos.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oldos.android.ui.components.*
import com.oldos.android.ui.components.ios4PinstripeBackground
import com.oldos.android.ui.components.rememberIOS4PinstripeBrush
import com.oldos.android.ui.components.IOS4StatusBarVariant

@Composable
fun GeneralSettingsScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .ios4PinstripeBackground(rememberIOS4PinstripeBrush())
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
        IOS4NavigationBar(
            title = "General",
            leftButtonText = "Settings",
            onLeftButtonClick = onBack,
            isLeftBackButton = true
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            IOS4GroupCard {
                IOS4RowItem(
                    title = "About",
                    showChevron = true,
                    onClick = { onNavigate("about") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            IOS4GroupCard {
                IOS4RowItem(
                    title = "Network",
                    showChevron = true,
                    onClick = { onNavigate("network") }
                )
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(
                    title = "Bluetooth",
                    detailText = if (com.oldos.android.model.SettingsManager.isBluetoothEnabled) "On" else "Off",
                    showChevron = true,
                    onClick = { onNavigate("bluetooth") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            IOS4GroupCard {
                IOS4RowItem(
                    title = "Auto-Lock",
                    detailText = "1 Minute",
                    showChevron = true,
                    onClick = { onNavigate("autolock") }
                )
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(
                    title = "Passcode Lock",
                    detailText = "Off",
                    showChevron = true,
                    onClick = { onNavigate("passcode") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            IOS4GroupCard {
                IOS4RowItem(
                    title = "Date & Time",
                    showChevron = true,
                    onClick = { onNavigate("datetime") }
                )
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(
                    title = "Keyboard",
                    showChevron = true,
                    onClick = { onNavigate("keyboard") }
                )
            }
        }
    }
}
