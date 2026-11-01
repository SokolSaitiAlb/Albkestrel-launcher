package com.albkestrel.launcher.data.repository

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import com.albkestrel.launcher.domain.model.AppCategory
import com.albkestrel.launcher.domain.model.LauncherApp
import com.albkestrel.launcher.domain.repository.AppRepository

class PackageManagerAppRepository(
    private val context: Context
) : AppRepository {

    // Curated AlbkestrelOS Living Flow Native Suite
    private val defaultAlbkestrelApps = listOf(
        LauncherApp(
            id = "app_phone",
            name = "Phone",
            packageName = "com.albkestrel.dialer",
            category = AppCategory.SOCIAL,
            iconSymbol = "phone",
            gradientColors = listOf(Color(0xFF059669), Color(0xFF10B981)),
            badgeCount = 2,
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_messages",
            name = "Messages",
            packageName = "com.albkestrel.messaging",
            category = AppCategory.SOCIAL,
            iconSymbol = "message",
            gradientColors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8)),
            badgeCount = 4,
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_browser",
            name = "Orbital",
            packageName = "com.albkestrel.browser",
            category = AppCategory.TOOLS,
            iconSymbol = "public",
            gradientColors = listOf(Color(0xFF4F46E5), Color(0xFF818CF8)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_camera",
            name = "Camera",
            packageName = "com.albkestrel.camera",
            category = AppCategory.MEDIA,
            iconSymbol = "camera",
            gradientColors = listOf(Color(0xFF1E293B), Color(0xFF475569)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_photos",
            name = "Photos",
            packageName = "com.albkestrel.gallery",
            category = AppCategory.MEDIA,
            iconSymbol = "photo_library",
            gradientColors = listOf(Color(0xFFE11D48), Color(0xFFF59E0B)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_music",
            name = "Acoustic",
            packageName = "com.albkestrel.music",
            category = AppCategory.MEDIA,
            iconSymbol = "music_note",
            gradientColors = listOf(Color(0xFF7C3AED), Color(0xFFC084FC)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_settings",
            name = "Settings",
            packageName = "com.albkestrel.settings",
            category = AppCategory.SYSTEM,
            iconSymbol = "settings",
            gradientColors = listOf(Color(0xFF0F172A), Color(0xFF334155)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_clock",
            name = "Chronos",
            packageName = "com.albkestrel.clock",
            category = AppCategory.TOOLS,
            iconSymbol = "schedule",
            gradientColors = listOf(Color(0xFF0369A1), Color(0xFF0284C7)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_weather",
            name = "Atmosphere",
            packageName = "com.albkestrel.weather",
            category = AppCategory.TOOLS,
            iconSymbol = "wb_sunny",
            gradientColors = listOf(Color(0xFF2563EB), Color(0xFF60A5FA)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_calendar",
            name = "Calendar",
            packageName = "com.albkestrel.calendar",
            category = AppCategory.PRODUCTIVITY,
            iconSymbol = "calendar_today",
            gradientColors = listOf(Color(0xFFDC2626), Color(0xFFF87171)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_files",
            name = "Files",
            packageName = "com.albkestrel.files",
            category = AppCategory.TOOLS,
            iconSymbol = "folder",
            gradientColors = listOf(Color(0xFFD97706), Color(0xFFFBBF24)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_notes",
            name = "Notes",
            packageName = "com.albkestrel.notes",
            category = AppCategory.PRODUCTIVITY,
            iconSymbol = "edit_note",
            gradientColors = listOf(Color(0xFF0D9488), Color(0xFF2DD4BF)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_health",
            name = "Vitality",
            packageName = "com.albkestrel.health",
            category = AppCategory.TOOLS,
            iconSymbol = "favorite",
            gradientColors = listOf(Color(0xFFBE185D), Color(0xFFFB7185)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_maps",
            name = "Compass",
            packageName = "com.albkestrel.maps",
            category = AppCategory.TOOLS,
            iconSymbol = "explore",
            gradientColors = listOf(Color(0xFF059669), Color(0xFF34D399)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_studio",
            name = "Albkestrel Studio",
            packageName = "com.albkestrel.studio",
            category = AppCategory.PRODUCTIVITY,
            iconSymbol = "auto_awesome",
            gradientColors = listOf(Color(0xFF0F172A), Color(0xFF00E5FF)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_calc",
            name = "Calculator",
            packageName = "com.albkestrel.calc",
            category = AppCategory.TOOLS,
            iconSymbol = "calculate",
            gradientColors = listOf(Color(0xFF334155), Color(0xFF64748B)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_recorder",
            name = "Voice Memo",
            packageName = "com.albkestrel.recorder",
            category = AppCategory.MEDIA,
            iconSymbol = "mic",
            gradientColors = listOf(Color(0xFF9333EA), Color(0xFFA855F7)),
            isSystemApp = true
        ),
        LauncherApp(
            id = "app_game_space",
            name = "Game Matrix",
            packageName = "com.albkestrel.gamespace",
            category = AppCategory.GAMES,
            iconSymbol = "sports_esports",
            gradientColors = listOf(Color(0xFFE11D48), Color(0xFF831843)),
            isSystemApp = true
        )
    )

    override fun loadAllApps(): List<LauncherApp> {
        val installed = queryInstalledLauncherApps()
        val combined = (defaultAlbkestrelApps + installed)
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase() }
        return combined
    }

    private fun queryInstalledLauncherApps(): List<LauncherApp> {
        return try {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            resolveInfos.mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                // Skip ourselves to avoid self-launch recursion
                if (pkgName == context.packageName) return@mapNotNull null

                val appName = resolveInfo.loadLabel(pm).toString()
                LauncherApp(
                    id = "installed_$pkgName",
                    name = appName,
                    packageName = pkgName,
                    category = AppCategory.TOOLS,
                    iconSymbol = "android",
                    gradientColors = listOf(Color(0xFF1E293B), Color(0xFF3B82F6)),
                    isInstalledApp = true,
                    isSystemApp = false
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun findAppByPackage(packageName: String): LauncherApp? {
        return loadAllApps().find { it.packageName == packageName }
    }

    override fun launchApp(app: LauncherApp): Boolean {
        return try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
