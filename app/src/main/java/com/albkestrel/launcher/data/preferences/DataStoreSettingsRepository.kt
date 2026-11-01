package com.albkestrel.launcher.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.albkestrel.launcher.domain.model.AlbkestrelThemeMode
import com.albkestrel.launcher.domain.model.AnimationSpeed
import com.albkestrel.launcher.domain.model.IconSize
import com.albkestrel.launcher.domain.model.LauncherSettings
import com.albkestrel.launcher.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "albkestrel_launcher_settings",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "albkestrel_launcher_prefs"
            )
        )
    }
)

class DataStoreSettingsRepository(
    private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val ICON_SIZE = stringPreferencesKey("icon_size")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val ANIMATION_SPEED = stringPreferencesKey("animation_speed")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SHOW_DOCK_LABELS = booleanPreferencesKey("show_dock_labels")
        val WALLPAPER_DIM = floatPreferencesKey("wallpaper_dim")
    }

    override val settings: Flow<LauncherSettings> = context.dataStore.data.map { prefs ->
        val iconSizeName = prefs[PreferencesKeys.ICON_SIZE] ?: IconSize.MEDIUM.name
        val gridCols = prefs[PreferencesKeys.GRID_COLUMNS] ?: 4
        val themeModeName = prefs[PreferencesKeys.THEME_MODE] ?: AlbkestrelThemeMode.DARK.name
        val dynamicColor = prefs[PreferencesKeys.DYNAMIC_COLOR] ?: false
        val animSpeedName = prefs[PreferencesKeys.ANIMATION_SPEED] ?: AnimationSpeed.FLUID_SPRING.name
        val haptics = prefs[PreferencesKeys.HAPTICS_ENABLED] ?: true
        val dockLabels = prefs[PreferencesKeys.SHOW_DOCK_LABELS] ?: false
        val wallpaperDim = prefs[PreferencesKeys.WALLPAPER_DIM] ?: 0.35f

        LauncherSettings(
            iconSize = runCatching { IconSize.valueOf(iconSizeName) }.getOrDefault(IconSize.MEDIUM),
            gridColumns = if (gridCols in 4..5) gridCols else 4,
            themeMode = runCatching { AlbkestrelThemeMode.valueOf(themeModeName) }.getOrDefault(AlbkestrelThemeMode.DARK),
            dynamicColor = dynamicColor,
            animationSpeed = runCatching { AnimationSpeed.valueOf(animSpeedName) }.getOrDefault(AnimationSpeed.FLUID_SPRING),
            hapticsEnabled = haptics,
            showDockLabels = dockLabels,
            wallpaperDim = wallpaperDim
        )
    }

    override suspend fun updateSettings(settings: LauncherSettings) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ICON_SIZE] = settings.iconSize.name
            prefs[PreferencesKeys.GRID_COLUMNS] = settings.gridColumns
            prefs[PreferencesKeys.THEME_MODE] = settings.themeMode.name
            prefs[PreferencesKeys.DYNAMIC_COLOR] = settings.dynamicColor
            prefs[PreferencesKeys.ANIMATION_SPEED] = settings.animationSpeed.name
            prefs[PreferencesKeys.HAPTICS_ENABLED] = settings.hapticsEnabled
            prefs[PreferencesKeys.SHOW_DOCK_LABELS] = settings.showDockLabels
            prefs[PreferencesKeys.WALLPAPER_DIM] = settings.wallpaperDim
        }
    }

    override suspend fun updateIconSize(iconSize: IconSize) {
        context.dataStore.edit { it[PreferencesKeys.ICON_SIZE] = iconSize.name }
    }

    override suspend fun updateGridColumns(columns: Int) {
        val validCols = if (columns in 4..5) columns else 4
        context.dataStore.edit { it[PreferencesKeys.GRID_COLUMNS] = validCols }
    }

    override suspend fun updateThemeMode(themeMode: AlbkestrelThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = themeMode.name }
    }

    override suspend fun updateAnimationSpeed(speed: AnimationSpeed) {
        context.dataStore.edit { it[PreferencesKeys.ANIMATION_SPEED] = speed.name }
    }

    override suspend fun toggleHaptics() {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.HAPTICS_ENABLED] ?: true
            prefs[PreferencesKeys.HAPTICS_ENABLED] = !current
        }
    }

    override suspend fun toggleDynamicColor() {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.DYNAMIC_COLOR] ?: false
            prefs[PreferencesKeys.DYNAMIC_COLOR] = !current
        }
    }

    override suspend fun toggleDockLabels() {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.SHOW_DOCK_LABELS] ?: false
            prefs[PreferencesKeys.SHOW_DOCK_LABELS] = !current
        }
    }

    override suspend fun updateWallpaperDim(dim: Float) {
        context.dataStore.edit { it[PreferencesKeys.WALLPAPER_DIM] = dim.coerceIn(0.0f, 0.9f) }
    }
}
