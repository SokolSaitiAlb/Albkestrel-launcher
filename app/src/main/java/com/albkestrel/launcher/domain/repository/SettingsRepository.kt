package com.albkestrel.launcher.domain.repository

import com.albkestrel.launcher.domain.model.AlbkestrelThemeMode
import com.albkestrel.launcher.domain.model.AnimationSpeed
import com.albkestrel.launcher.domain.model.IconSize
import com.albkestrel.launcher.domain.model.LauncherSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<LauncherSettings>
    suspend fun updateSettings(settings: LauncherSettings)
    suspend fun updateIconSize(iconSize: IconSize)
    suspend fun updateGridColumns(columns: Int)
    suspend fun updateThemeMode(themeMode: AlbkestrelThemeMode)
    suspend fun updateAnimationSpeed(speed: AnimationSpeed)
    suspend fun toggleHaptics()
    suspend fun toggleDynamicColor()
    suspend fun toggleDockLabels()
    suspend fun updateWallpaperDim(dim: Float)
}
