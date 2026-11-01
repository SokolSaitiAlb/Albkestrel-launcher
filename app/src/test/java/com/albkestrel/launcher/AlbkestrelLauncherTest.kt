package com.albkestrel.launcher

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.albkestrel.launcher.data.preferences.DataStoreSettingsRepository
import com.albkestrel.launcher.data.repository.PackageManagerAppRepository
import com.albkestrel.launcher.domain.model.AlbkestrelThemeMode
import com.albkestrel.launcher.domain.model.AnimationSpeed
import com.albkestrel.launcher.domain.model.IconSize
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AlbkestrelLauncherTest {

    @Test
    fun `app string and package manager basic verification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AlbkestrelOS", appName)

        val appRepository = PackageManagerAppRepository(context)
        val apps = appRepository.loadAllApps()
        assertTrue(apps.isNotEmpty())
        assertTrue(apps.any { it.name == "Phone" })
        assertTrue(apps.any { it.name == "Messages" })
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
