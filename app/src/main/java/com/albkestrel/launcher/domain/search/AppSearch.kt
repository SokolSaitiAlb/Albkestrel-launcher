package com.albkestrel.launcher.domain.search

import com.albkestrel.launcher.domain.model.AppCategory
import com.albkestrel.launcher.domain.model.LauncherApp

/**
 * Pure filtering and ordering helpers shared by the app drawer and the search overlay.
 * Keeping them outside Compose makes the behaviour deterministic and unit-testable.
 */
object AppSearch {

    private val alphabeticalOrder =
        compareBy(String.CASE_INSENSITIVE_ORDER) { app: LauncherApp -> app.name }
            .thenBy { it.packageName }

    fun sortAlphabetically(apps: List<LauncherApp>): List<LauncherApp> {
        return apps.sortedWith(alphabeticalOrder)
    }

    fun filterByCategory(apps: List<LauncherApp>, category: AppCategory): List<LauncherApp> {
        return if (category == AppCategory.ALL) {
            apps
        } else {
            apps.filter { it.category == category }
        }
    }

    /**
     * Filters installed applications by their visible label. A blank query keeps every app,
     * which is what the app drawer needs while the search overlay treats blank separately.
     */
    fun filterByLabel(apps: List<LauncherApp>, query: String): List<LauncherApp> {
        val trimmed = query.trim()
        val matches = if (trimmed.isEmpty()) {
            apps
        } else {
            apps.filter { it.name.contains(trimmed, ignoreCase = true) }
        }
        return sortAlphabetically(matches)
    }
}
