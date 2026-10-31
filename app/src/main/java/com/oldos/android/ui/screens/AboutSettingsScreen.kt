package com.oldos.android.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.oldos.android.ui.components.*
import com.oldos.android.ui.components.ios4PinstripeBackground
import com.oldos.android.ui.components.rememberIOS4PinstripeBrush
import com.oldos.android.ui.components.IOS4StatusBarVariant

@Composable
fun AboutSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val deviceStats = remember {
        // Real capacities from the data partition
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalGb = stat.totalBytes / (1024.0 * 1024.0 * 1024.0)
        val availableGb = stat.availableBytes / (1024.0 * 1024.0 * 1024.0)
        DeviceStats(
            totalGb = "%.1f GB".format(totalGb),
            availableGb = "%.1f GB".format(availableGb)
        )
    }

    val songs = remember {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) ==
            PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            countMedia(context, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        } else null
    }
    val photos = remember {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ==
            PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            countMedia(context, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        } else null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .ios4PinstripeBackground(rememberIOS4PinstripeBrush())
    ) {
        IOS4StatusBar(variant = IOS4StatusBarVariant.Light)
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
                IOS4RowItem(
                    title = "Network",
                    detailText = com.oldos.android.model.SettingsManager.carrierName
                )
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Songs", detailText = songs?.toString() ?: "—")
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Photos", detailText = photos?.toString() ?: "—")
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Applications", detailText = "24")
            }

            Spacer(modifier = Modifier.height(10.dp))

            IOS4GroupCard {
                IOS4RowItem(title = "Capacity", detailText = deviceStats.totalGb)
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Available", detailText = deviceStats.availableGb)
            }

            Spacer(modifier = Modifier.height(10.dp))

            IOS4GroupCard {
                IOS4RowItem(title = "Version", detailText = "4.3.3 (8J2)")
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(
                    title = "Carrier",
                    detailText = com.oldos.android.model.SettingsManager.carrierName
                )
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Model", detailText = Build.MODEL)
                IOS4RowDivider(hasIcon = false)
                IOS4RowItem(title = "Android", detailText = "API ${Build.VERSION.SDK_INT}")
            }
        }
    }
}

private data class DeviceStats(val totalGb: String, val availableGb: String)

private fun countMedia(context: android.content.Context, uri: android.net.Uri): Int? = try {
    context.contentResolver.query(uri, arrayOf("count(*)"), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getInt(0) else 0
    }
} catch (_: Exception) {
    null
}
