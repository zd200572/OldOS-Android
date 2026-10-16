package com.oldos.android.model

import androidx.annotation.DrawableRes

data class AppItem(
    val id: String,
    val title: String,
    @DrawableRes val iconRes: Int,
    val badgeCount: Int = 0,
    val isSystemApp: Boolean = true,
    val isFolder: Boolean = false
)
