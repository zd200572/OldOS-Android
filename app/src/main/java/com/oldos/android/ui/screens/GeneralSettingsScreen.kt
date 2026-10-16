package com.oldos.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oldos.android.ui.components.*

@Composable
fun GeneralSettingsScreen(
    onNavigateToAbout: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFC5CCD4))
    ) {
        IOS4StatusBar()
        IOS4NavigationBar(
            title = "General",
            leftButtonText = "Settings",
            onLeftButtonClick = onBack
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
                    onClick = onNavigateToAbout
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            IOS4GroupCard {
                IOS4RowItem(
                    title = "Network",
                    showChevron = true,
                    onClick = {}
                )
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(
                    title = "Bluetooth",
                    detailText = "On",
                    showChevron = true,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            IOS4GroupCard {
                IOS4RowItem(
                    title = "Auto-Lock",
                    detailText = "1 Minute",
                    showChevron = true,
                    onClick = {}
                )
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(
                    title = "Passcode Lock",
                    detailText = "Off",
                    showChevron = true,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            IOS4GroupCard {
                IOS4RowItem(
                    title = "Date & Time",
                    showChevron = true,
                    onClick = {}
                )
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(
                    title = "Keyboard",
                    showChevron = true,
                    onClick = {}
                )
            }
        }
    }
}
