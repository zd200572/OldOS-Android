package com.oldos.android.model

import androidx.compose.runtime.mutableStateListOf
import com.oldos.android.R

object AppHistoryManager {
    val recentApps = mutableStateListOf(
        AppItem("settings", "Settings", R.drawable.ic_settings),
        AppItem("calculator", "Calculator", R.drawable.ic_calculator),
        AppItem("safari", "Safari", R.drawable.ic_safari),
        AppItem("ipod", "iPod", R.drawable.ic_ipod),
        AppItem("mail", "Mail", R.drawable.ic_mail, badgeCount = 2),
        AppItem("photos", "Photos", R.drawable.ic_photos),
        AppItem("phone", "Phone", R.drawable.ic_phone),
        AppItem("notes", "Notes", R.drawable.ic_notes),
        AppItem("maps", "Maps", R.drawable.ic_maps)
    )

    fun onAppLaunched(app: AppItem) {
        val existingIndex = recentApps.indexOfFirst { it.id == app.id }
        if (existingIndex >= 0) {
            recentApps.removeAt(existingIndex)
        }
        recentApps.add(0, app)
    }

    fun killApp(appId: String) {
        recentApps.removeAll { it.id == appId }
    }
}
