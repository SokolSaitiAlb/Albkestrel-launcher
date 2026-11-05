package com.albkestrel.launcher.domain.search

import com.albkestrel.launcher.domain.model.AppCategory
import com.albkestrel.launcher.domain.model.LauncherApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSearchTest {

    private val apps = listOf(
        app("Zeta", "com.example.zeta", AppCategory.TOOLS),
        app("alpha", "com.example.alpha", AppCategory.PRODUCTIVITY),
        app("Beta", "com.example.beta", AppCategory.MEDIA)
    )

    @Test
    fun `filters by label case insensitively`() {
        assertEquals(listOf("alpha"), AppSearch.filterByLabel(apps, "ALP").map { it.name })
        assertEquals(listOf("Beta"), AppSearch.filterByLabel(apps, "bet").map { it.name })
    }

    @Test
    fun `blank query returns every app in alphabetical order`() {
        assertEquals(
            listOf("alpha", "Beta", "Zeta"),
            AppSearch.filterByLabel(apps, "   ").map { it.name }
        )
    }

    @Test
    fun `unmatched query returns an empty list`() {
        assertTrue(AppSearch.filterByLabel(apps, "missing").isEmpty())
    }

    @Test
    fun `category filter can be combined with label filtering`() {
        val media = AppSearch.filterByCategory(apps, AppCategory.MEDIA)
        assertEquals(listOf("Beta"), AppSearch.filterByLabel(media, "e").map { it.name })
    }

    private fun app(name: String, packageName: String, category: AppCategory): LauncherApp {
        return LauncherApp(
            id = "test_$packageName",
            name = name,
            packageName = packageName,
            category = category,
            isInstalledApp = true
        )
    }
}
