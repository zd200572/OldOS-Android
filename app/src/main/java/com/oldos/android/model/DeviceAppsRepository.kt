package com.oldos.android.model

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap

data class DeviceApp(
    val label: String,
    val packageName: String
)

/**
 * Real installed Android apps, queryable from the SpringBoard. Uses the
 * manifest <queries> declaration (MAIN + LAUNCHER) so no broad
 * QUERY_ALL_PACKAGES permission is required.
 */
object DeviceAppsRepository {

    fun loadDeviceApps(context: Context): List<DeviceApp> {
        return try {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            pm.queryIntentActivities(intent, 0)
                .asSequence()
                .map { DeviceApp(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
                .filter { it.packageName != context.packageName }
                .distinctBy { it.packageName }
                .sortedBy { it.label.lowercase() }
                .toList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** Real app icon rendered at the requested size. */
    fun loadIcon(context: Context, app: DeviceApp, sizePx: Int): Bitmap? {
        return try {
            context.packageManager.getApplicationIcon(app.packageName).toBitmap(sizePx, sizePx)
        } catch (_: Exception) {
            null
        }
    }

    fun launchIntent(context: Context, app: DeviceApp): Intent? {
        return context.packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
