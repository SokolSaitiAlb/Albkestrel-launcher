package com.albkestrel.launcher.data.repository

import com.albkestrel.launcher.domain.model.AppCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InstalledAppsMappingTest {

    @Test
    fun `maps installed launcher activities to launcher apps`() {
        val apps = mapLauncherActivities(
            listOf(
                record(
                    packageName = "com.example.productivity",
                    activityName = "com.example.productivity.MainActivity",
                    label = "Tasks",
                    category = AppCategory.PRODUCTIVITY,
                    isSystemApp = true
                )
            ),
            selfPackageName = "com.example.launcher"
        )

        assertEquals(1, apps.size)
        val app = apps.first()
        assertEquals("Tasks", app.name)
        assertEquals("com.example.productivity", app.packageName)
        assertEquals("com.example.productivity.MainActivity", app.activityName)
        assertEquals(AppCategory.PRODUCTIVITY, app.category)
        assertTrue(app.isInstalledApp)
        assertTrue(app.isSystemApp)
    }

    @Test
    fun `excludes the launcher itself`() {
        val apps = mapLauncherActivities(
            listOf(
                record("com.example.launcher", "com.example.launcher.MainActivity", "Launcher"),
                record("com.example.maps", "com.example.maps.MainActivity", "Maps")
            ),
            selfPackageName = "com.example.launcher"
        )

        assertEquals(listOf("Maps"), apps.map { it.name })
    }

    @Test
    fun `sorts labels case insensitively with package tie-breaker`() {
        val apps = mapLauncherActivities(
            listOf(
                record("com.example.zeta", "com.example.zeta.MainActivity", "Zeta"),
                record("com.example.alpha", "com.example.alpha.MainActivity", "alpha"),
                record("com.example.beta", "com.example.beta.MainActivity", "Beta"),
                record("com.example.beta2", "com.example.beta2.MainActivity", "Beta")
            ),
            selfPackageName = "com.example.launcher"
        )

        assertEquals(
            listOf("alpha", "Beta", "Beta", "Zeta"),
            apps.map { it.name }
        )
        assertEquals(
            listOf("com.example.alpha", "com.example.beta", "com.example.beta2", "com.example.zeta"),
            apps.map { it.packageName }
        )
    }

    @Test
    fun `deduplicates multiple launcher activities in the same package`() {
        val apps = mapLauncherActivities(
            listOf(
                record(
                    "com.example.multi",
                    "com.example.multi.SecondActivity",
                    "Multi"
                ),
                record(
                    "com.example.multi",
                    "com.example.multi.MainActivity",
                    "Multi"
                )
            ),
            selfPackageName = "com.example.launcher"
        )

        assertEquals(1, apps.size)
        assertEquals("com.example.multi.MainActivity", apps.first().activityName)
        assertEquals("installed_com.example.multi", apps.first().id)
    }

    @Test
    fun `real installed apps never use legacy mock widget identifiers`() {
        val apps = mapLauncherActivities(
            listOf(
                record(
                    packageName = "com.albkestrel.clock",
                    activityName = "com.albkestrel.clock.ClockActivity",
                    label = "Clock"
                )
            ),
            selfPackageName = "com.albkestrel.launcher"
        )

        assertEquals(1, apps.size)
        assertEquals("installed_com.albkestrel.clock", apps.first().id)
        assertTrue(apps.none { it.id.startsWith("app_") })
    }

    @Test
    fun `empty resolve results produce an empty app list`() {
        val apps = mapLauncherActivities(
            activities = emptyList(),
            selfPackageName = "com.example.launcher"
        )

        assertTrue(apps.isEmpty())
    }

    @Test
    fun `blank labels fall back to the package name`() {
        val apps = mapLauncherActivities(
            listOf(record("com.example.blank", "com.example.blank.MainActivity", "   ")),
            selfPackageName = "com.example.launcher"
        )

        assertEquals("com.example.blank", apps.first().name)
    }

    private fun record(
        packageName: String,
        activityName: String,
        label: String,
        category: AppCategory = AppCategory.TOOLS,
        isSystemApp: Boolean = false
    ): LauncherActivityRecord {
        return LauncherActivityRecord(
            packageName = packageName,
            activityName = activityName,
            label = label,
            icon = null,
            category = category,
            isSystemApp = isSystemApp
        )
    }
}
