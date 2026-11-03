package com.albkestrel.launcher.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.albkestrel.launcher.domain.model.AppCategory
import com.albkestrel.launcher.domain.model.LauncherApp
import com.albkestrel.launcher.domain.search.AppSearch

/**
 * Lightweight representation of a single launchable activity resolved through PackageManager.
 * Keeping this separate from [android.content.pm.ResolveInfo] makes the mapping deterministic and
 * testable without touching Android resources.
 */
internal data class LauncherActivityRecord(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: ImageBitmap?,
    val category: AppCategory = AppCategory.TOOLS,
    val isSystemApp: Boolean = false
)

/**
 * Maps resolved launcher activities to the launcher domain model.
 *
 * - The launcher itself is excluded.
 * - A package with several launcher activities is represented once, using the
 *   alphabetically first activity so the result is stable across queries.
 * - Results are sorted case-insensitively by label, with package name as a tie-breaker.
 */
internal fun mapLauncherActivities(
    activities: List<LauncherActivityRecord>,
    selfPackageName: String
): List<LauncherApp> {
    return activities
        .asSequence()
        .filter { record ->
            record.packageName.isNotBlank() &&
                record.activityName.isNotBlank() &&
                record.packageName != selfPackageName
        }
        .sortedWith(
            compareBy<LauncherActivityRecord> { it.packageName }
                .thenBy { it.activityName }
        )
        .distinctBy { it.packageName }
        .map { record ->
            LauncherApp(
                id = "installed_${record.packageName}",
                name = record.label.ifBlank { record.packageName },
                packageName = record.packageName,
                activityName = record.activityName,
                category = record.category,
                iconSymbol = "android",
                icon = record.icon,
                gradientColors = installedAppGradient(record.packageName),
                isSystemApp = record.isSystemApp,
                isInstalledApp = true
            )
        }
        .toList()
        .let(AppSearch::sortAlphabetically)
}

private val installedGradients = listOf(
    listOf(Color(0xFF1E293B), Color(0xFF3B82F6)),
    listOf(Color(0xFF0F766E), Color(0xFF14B8A6)),
    listOf(Color(0xFF7C3AED), Color(0xFFA78BFA)),
    listOf(Color(0xFF9D174D), Color(0xFFF472B6)),
    listOf(Color(0xFF9A3412), Color(0xFFFB923C))
)

private fun installedAppGradient(packageName: String): List<Color> {
    val palette = installedGradients[(packageName.hashCode() and Int.MAX_VALUE) % installedGradients.size]
    return palette
}
