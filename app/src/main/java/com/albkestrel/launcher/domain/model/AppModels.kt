package com.albkestrel.launcher.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AlbkestrelThemeMode {
    SYSTEM,
    DARK,
    LIGHT,
    EMERALD
}

enum class AppCategory(val displayName: String) {
    ALL("All"),
    PRODUCTIVITY("Work"),
    MEDIA("Media"),
    TOOLS("Utilities"),
    SOCIAL("Social"),
    GAMES("Games"),
    SYSTEM("System")
}

enum class IconSize(val dp: Dp, val label: String) {
    SMALL(48.dp, "Compact (48dp)"),
    MEDIUM(56.dp, "Balanced (56dp)"),
    LARGE(64.dp, "Expansive (64dp)")
}

enum class AnimationSpeed(val label: String, val description: String) {
    FLUID_SPRING("Living Flow Spring", "Natural spring physics with fluid damping"),
    SNAPPY("Snappy", "Crisp high-speed transitions"),
    REDUCED_MOTION("Reduced Motion", "Minimal movement with instant fades for accessibility")
}

data class LauncherApp(
    val id: String,
    val name: String,
    val packageName: String,
    val category: AppCategory = AppCategory.TOOLS,
    val iconSymbol: String = "apps",
    val gradientColors: List<Color> = listOf(Color(0xFF1E293B), Color(0xFF334155)),
    val badgeCount: Int? = null,
    val isSystemApp: Boolean = false,
    val isInstalledApp: Boolean = false,
    val installTimeMillis: Long = System.currentTimeMillis()
)

data class LauncherSettings(
    val iconSize: IconSize = IconSize.MEDIUM,
    val gridColumns: Int = 4,
    val themeMode: AlbkestrelThemeMode = AlbkestrelThemeMode.DARK,
    val dynamicColor: Boolean = false,
    val animationSpeed: AnimationSpeed = AnimationSpeed.FLUID_SPRING,
    val hapticsEnabled: Boolean = true,
    val showDockLabels: Boolean = false,
    val wallpaperDim: Float = 0.35f
)

data class WeatherMockData(
    val temperature: Int = 22,
    val condition: String = "Clear Sky",
    val city: String = "San Francisco",
    val highTemp: Int = 26,
    val lowTemp: Int = 15,
    val humidity: Int = 52,
    val airQuality: String = "Good (34 AQI)",
    val notice: String = "MOCK WEATHER DATA"
)
