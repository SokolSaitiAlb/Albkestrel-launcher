package com.albkestrel.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import androidx.test.core.app.ApplicationProvider
import com.albkestrel.launcher.data.preferences.DataStoreSettingsRepository
import com.albkestrel.launcher.data.repository.PackageManagerAppRepository
import com.albkestrel.launcher.domain.model.AlbkestrelThemeMode
import com.albkestrel.launcher.domain.model.AnimationSpeed
import com.albkestrel.launcher.domain.model.AppCategory
import com.albkestrel.launcher.domain.model.IconSize
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AlbkestrelLauncherTest {

    @Test
    fun `package manager repository maps installed launcher apps and excludes itself`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager

        val applicationInfo = ApplicationInfo().apply {
            packageName = "com.example.zeta"
            name = "Zeta"
            flags = 0
            category = ApplicationInfo.CATEGORY_PRODUCTIVITY
        }
        val activityInfo = ActivityInfo().apply {
            packageName = "com.example.zeta"
            name = "com.example.zeta.MainActivity"
            this.applicationInfo = applicationInfo
            nonLocalizedLabel = "Zeta App"
            icon = android.R.drawable.sym_def_app_icon
        }
        val resolveInfo = ResolveInfo().apply {
            this.activityInfo = activityInfo
            nonLocalizedLabel = "Zeta App"
        }

        Shadows.shadowOf(packageManager).addResolveInfoForIntent(
            Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) },
            resolveInfo
        )

        val appRepository = PackageManagerAppRepository(context)
        val apps = appRepository.loadAllApps()
        val zeta = apps.find { it.packageName == "com.example.zeta" }

        assertNotNull(zeta)
        assertEquals("Zeta App", zeta?.name)
        assertEquals("com.example.zeta.MainActivity", zeta?.activityName)
        assertEquals(AppCategory.PRODUCTIVITY, zeta?.category)
        assertTrue(zeta?.isInstalledApp == true)
        assertFalse(apps.any { it.packageName == context.packageName })

        val labels = apps.map { it.name.lowercase() }
        assertEquals(labels.sorted(), labels)
    }

    @Test
    fun `datastore settings defaults and updates`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsRepo = DataStoreSettingsRepository(context)

        val initialSettings = settingsRepo.settings.first()
        assertEquals(4, initialSettings.gridColumns)
        assertEquals(IconSize.MEDIUM, initialSettings.iconSize)

        // Update settings
        settingsRepo.updateGridColumns(5)
        settingsRepo.updateIconSize(IconSize.LARGE)
        settingsRepo.updateThemeMode(AlbkestrelThemeMode.EMERALD)
        settingsRepo.updateAnimationSpeed(AnimationSpeed.REDUCED_MOTION)

        val updated = settingsRepo.settings.first()
        assertEquals(5, updated.gridColumns)
        assertEquals(IconSize.LARGE, updated.iconSize)
        assertEquals(AlbkestrelThemeMode.EMERALD, updated.themeMode)
        assertEquals(AnimationSpeed.REDUCED_MOTION, updated.animationSpeed)
    }
}
