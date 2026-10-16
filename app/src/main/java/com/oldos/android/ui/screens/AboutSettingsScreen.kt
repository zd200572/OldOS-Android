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
fun AboutSettingsScreen(
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
            title = "About",
            leftButtonText = "General",
            onLeftButtonClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            IOS4GroupCard {
                IOS4RowItem(title = "Network", detailText = "中国移动")
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Songs", detailText = "128")
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Videos", detailText = "12")
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Photos", detailText = "256")
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Applications", detailText = "24")
            }

            Spacer(modifier = Modifier.height(10.dp))

            IOS4GroupCard {
                IOS4RowItem(title = "Capacity", detailText = "32 GB")
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Available", detailText = "28.4 GB")
            }

            Spacer(modifier = Modifier.height(10.dp))

            IOS4GroupCard {
                IOS4RowItem(title = "Version", detailText = "4.3.3 (8J2)")
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Carrier", detailText = "中国移动 10.0")
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Model", detailText = "MC603CH")
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Wi-Fi Address", detailText = "00:25:00:A1:B2:C3")
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Bluetooth", detailText = "00:25:00:D4:E5:F6")
            }
        }
    }
}
