package com.albkestrel.launcher

import com.albkestrel.launcher.domain.contextmenu.AppContextMenuPolicy
import com.albkestrel.launcher.domain.contextmenu.AppShortcut
import com.albkestrel.launcher.domain.model.LauncherApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppContextMenuPolicyTest {

    private val installedApp = LauncherApp(
        id = "app_gallery",
        name = "Gallery",
        packageName = "com.example.gallery",
        activityName = "com.example.gallery.MainActivity",
        isInstalledApp = true
    )

    private val systemApp = LauncherApp(
        id = "app_mail",
        name = "Mail",
        packageName = "com.example.mail",
        activityName = "com.example.mail.MainActivity",
        isInstalledApp = true,
        isSystemApp = true
    )

    private val removedApp = installedApp.copy(isInstalledApp = false)

    @Test
    fun `context menu for installed app exposes info shortcuts remove and uninstall actions`() {
        val actions = AppContextMenuPolicy.actionsFor(
            installedApp,
            listOf(
                AppShortcut(
                    id = "com.example.gallery.camera",
                    packageName = "com.example.gallery",
                    activityName = "com.example.gallery.CameraActivity",
                    shortLabel = "Camera",
                    longLabel = "Camera"
                )
            )
        )

        assertEquals(
            listOf(
                AppContextMenuPolicy.ACTION_APP_INFO,
                "shortcut:com.example.gallery.camera",
                AppContextMenuPolicy.ACTION_REMOVE,
                AppContextMenuPolicy.ACTION_UNINSTALL
            ),
            actions.map { it.id }
        )
    }

    @Test
    fun `context menu for missing or removed app exposes no actions`() {
        val actions = AppContextMenuPolicy.actionsFor(removedApp, emptyList())

        assertTrue("Context menu must never offer actions for packages that are no longer installed", actions.isEmpty())
    }

    @Test
    fun `context menu for system app omits uninstall and keeps remove available`() {
        val actions = AppContextMenuPolicy.actionsFor(systemApp, emptyList())
        val hasAppInfo = actions.any { it.id == AppContextMenuPolicy.ACTION_APP_INFO }
        val hasRemove = actions.any { it.id == AppContextMenuPolicy.ACTION_REMOVE }
        val hasUninstall = actions.any { it.id == AppContextMenuPolicy.ACTION_UNINSTALL }

        assertTrue("System apps should still expose app info", hasAppInfo)
        assertTrue("Launcher workspace icons should still be removable for system apps", hasRemove)
        assertFalse("System packages must never be offered for uninstall", hasUninstall)
    }

    @Test
    fun `pause and widget integrations are unavailable and never surface menu actions`() {
        assertFalse("Digital Wellbeing does not expose a public per-app pause API usable by a launcher yet", AppContextMenuPolicy.isPauseSupported())
        assertFalse("No real AppWidgetHost integration exists yet, so no widget picker can be rendered", AppContextMenuPolicy.isWidgetSupportAvailable())

        val actionIds = AppContextMenuPolicy.actionsFor(installedApp, emptyList()).map { it.id }

        assertFalse(AppContextMenuPolicy.ACTION_PAUSE_APP in actionIds)
        assertFalse(AppContextMenuPolicy.ACTION_WIDGETS in actionIds)
    }

    @Test
    fun `shortcut actions are limited to shortcuts discovered for the selected app`() {
        val actions = AppContextMenuPolicy.actionsFor(
            installedApp,
            listOf(
                AppShortcut(
                    id = "com.example.gallery.camera",
                    packageName = "com.example.gallery",
                    activityName = null,
                    shortLabel = "Camera",
                    longLabel = "Camera"
                ),
                AppShortcut(
                    id = "com.example.weather.reminders",
                    packageName = "com.example.weather",
                    activityName = null,
                    shortLabel = "Reminders",
                    longLabel = "Reminders"
                )
            )
        )

        val shortcutActions = actions.filter { it.isShortcutAction }

        assertEquals(1, shortcutActions.size)
        assertEquals("com.example.gallery.camera", shortcutActions.first().shortcutId)
        assertEquals("Camera", shortcutActions.first().title)
    }

    @Test
    fun `uninstall is blocked for system packages`() {
        assertTrue(AppContextMenuPolicy.isUninstallBlocked(systemApp))
        assertFalse(AppContextMenuPolicy.isUninstallBlocked(installedApp))
    }

    @Test
    fun `remove and uninstall are distinct actions`() {
        val actions = AppContextMenuPolicy.actionsFor(installedApp, emptyList())
        val removeAction = actions.firstOrNull { it.id == AppContextMenuPolicy.ACTION_REMOVE }
        val uninstallAction = actions.firstOrNull { it.id == AppContextMenuPolicy.ACTION_UNINSTALL }

        assertEquals(1, actions.count { it.id == AppContextMenuPolicy.ACTION_REMOVE })
        assertEquals(1, actions.count { it.id == AppContextMenuPolicy.ACTION_UNINSTALL })
        assertEquals(AppContextMenuPolicy.ACTION_REMOVE, removeAction?.id)
        assertEquals(AppContextMenuPolicy.ACTION_UNINSTALL, uninstallAction?.id)
        assertEquals(false, removeAction?.isShortcutAction)
        assertEquals(false, uninstallAction?.isShortcutAction)
    }
}
