package com.albkestrel.launcher.data.repository

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.albkestrel.launcher.domain.model.LauncherApp
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PackageManagerAppRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = PackageManagerAppRepository(context)

    @Test
    fun `builds an explicit launch intent for the discovered activity`() {
        val app = LauncherApp(
            id = "installed_com.example.app",
            name = "Example",
            packageName = "com.example.app",
            activityName = "com.example.app.LauncherActivity",
            isInstalledApp = true
        )

        val intent = repository.buildLaunchIntent(app)

        assertNotNull(intent)
        assertEquals(Intent.ACTION_MAIN, intent!!.action)
        assertTrue(intent.hasCategory(Intent.CATEGORY_LAUNCHER))
        assertEquals(
            ComponentName("com.example.app", "com.example.app.LauncherActivity"),
            intent.component
        )
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `does not build a launch intent for unavailable apps`() {
        val app = LauncherApp(
            id = "missing",
            name = "Missing",
            packageName = "com.missing.app",
            isInstalledApp = false
        )

        assertNull(repository.buildLaunchIntent(app))
        assertFalse(repository.launchApp(app))
    }

    @Test
    fun `package add broadcast emits a refresh before the receiver invalidates it`() = runBlocking {
        val emissions = mutableListOf<Unit>()
        val job = launch { repository.packageChanges.collect { emissions.add(it) } }

        context.sendBroadcast(
            Intent(Intent.ACTION_PACKAGE_ADDED).apply {
                data = Uri.parse("package:com.example.newapp")
            }
        )
        delay(100)
        shadowOf(Looper.getMainLooper()).idle()
        delay(100)
        job.cancel()

        assertEquals(1, emissions.size)
    }

    @Test
    fun `installed app without a launchable activity fails safely`() {
        val app = LauncherApp(
            id = "installed_com.missing.app",
            name = "Missing",
            packageName = "com.missing.app",
            isInstalledApp = true
        )

        assertNull(repository.buildLaunchIntent(app))
        assertFalse(repository.launchApp(app))
    }

    @Test
    fun `openAppDetails returns false for uninstalled app`() {
        assertFalse(repository.openAppDetails("com.nonexistent.app"))
    }

    @Test
    fun `uninstallApp returns false for uninstalled app`() {
        assertFalse(repository.uninstallApp("com.nonexistent.app"))
    }

    @Test
    fun `uninstallApp refuses system packages even when they are installed`() {
        installLauncherPackage("com.example.system", isSystem = true)

        assertFalse(repository.uninstallApp("com.example.system"))
    }

    @Test
    fun `uninstallApp initiates the official delete flow for user packages`() {
        installLauncherPackage("com.example.user", isSystem = false)

        assertTrue(repository.uninstallApp("com.example.user"))

        val started = shadowOf(context as Application).nextStartedActivity
        assertEquals(Intent.ACTION_DELETE, started?.action)
        assertEquals("package:com.example.user", started?.dataString)
    }

    @Test
    fun `getShortcutsForPackage returns empty list when shortcuts are unavailable`() {
        val shortcuts = repository.getShortcutsForPackage("com.example.app")
        assertTrue(shortcuts.isEmpty())
    }

    @Test
    fun `launchShortcut returns false when shortcut does not exist`() {
        val app = LauncherApp(
            id = "installed_com.example.app",
            name = "Example",
            packageName = "com.example.app",
            isInstalledApp = true
        )
        assertFalse(repository.launchShortcut(app, "missing_shortcut_id"))
    }

    @Test
    fun `launchShortcut rejects blank ids and uninstalled apps`() {
        val app = LauncherApp(
            id = "installed_com.example.app",
            name = "Example",
            packageName = "com.example.app",
            isInstalledApp = true
        )

        assertFalse(repository.launchShortcut(app, ""))
        assertFalse(repository.launchShortcut(app.copy(isInstalledApp = false), "valid_shortcut_id"))
    }

    private fun installLauncherPackage(packageName: String, isSystem: Boolean) {
        val applicationInfo = ApplicationInfo().apply {
            this.packageName = packageName
            nonLocalizedLabel = packageName.substringAfterLast('.')
            flags = if (isSystem) ApplicationInfo.FLAG_SYSTEM else 0
        }
        val activityInfo = ActivityInfo().apply {
            this.packageName = packageName
            name = "$packageName.MainActivity"
            this.applicationInfo = applicationInfo
        }
        val resolveInfo = ResolveInfo().apply {
            this.activityInfo = activityInfo
        }
        shadowOf(context.packageManager).addResolveInfoForIntent(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
            resolveInfo
        )
    }
}
