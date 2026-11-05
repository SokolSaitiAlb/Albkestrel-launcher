package com.albkestrel.launcher

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import com.albkestrel.launcher.app.AlbkestrelApplication
import com.albkestrel.launcher.domain.model.LauncherApp
import com.albkestrel.launcher.feature.common.LauncherOverlay
import com.albkestrel.launcher.feature.common.LauncherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LauncherAppContextMenuTest {

    private lateinit var application: AlbkestrelApplication
    private lateinit var viewModel: LauncherViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val installedApp = LauncherApp(
        id = "app_gallery",
        name = "Gallery",
        packageName = "com.example.gallery",
        activityName = "com.example.gallery.MainActivity",
        isInstalledApp = true
    )

    private val removedApp = installedApp.copy(
        id = "missing_com.example.gallery",
        name = "Gallery (removed)",
        isInstalledApp = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        viewModel = LauncherViewModel(application)
    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
        }
        Dispatchers.resetMain()
    }

    @Test
    fun `long press on installed app opens anchored context menu overlay`() = runTest(testDispatcher) {
        viewModel.closeOverlay()

        viewModel.onAppLongPressed(installedApp, Offset(x = 137f, y = 89f))

        val overlay = viewModel.activeOverlay.value
        assertTrue("Long press should open the app context menu overlay", overlay is LauncherOverlay.AppContextMenu)
        val contextMenu = overlay as LauncherOverlay.AppContextMenu
        assertEquals("Context menu must reference the long-pressed app", installedApp, contextMenu.app)
        assertEquals("Context menu must be anchored near the pressed icon", 137f, contextMenu.anchorPosition.x, 0.001f)
        assertEquals("Context menu must be anchored near the pressed icon", 89f, contextMenu.anchorPosition.y, 0.001f)
    }

    @Test
    fun `long press on removed app does not open context menu`() = runTest(testDispatcher) {
        viewModel.closeOverlay()

        viewModel.onAppLongPressed(removedApp, Offset(x = 10f, y = 20f))

        assertEquals("Stale references to removed packages must not open a context menu", LauncherOverlay.None, viewModel.activeOverlay.value)
    }

    @Test
    fun `context menu dismissal clears overlay state`() = runTest(testDispatcher) {
        viewModel.onAppLongPressed(installedApp, Offset(x = 5f, y = 6f))
        assertTrue(viewModel.activeOverlay.value is LauncherOverlay.AppContextMenu)

        viewModel.dismissAppContextMenu()

        assertEquals(LauncherOverlay.None, viewModel.activeOverlay.value)
    }

    @Test
    fun `context menu dismissal is a safe no-op without an open menu`() = runTest(testDispatcher) {
        viewModel.closeOverlay()
        viewModel.dismissAppContextMenu()

        assertEquals(LauncherOverlay.None, viewModel.activeOverlay.value)
    }

    @Test
    fun `context menu remove action dismisses the menu overlay`() = runTest(testDispatcher) {
        viewModel.onAppLongPressed(installedApp, Offset(x = 5f, y = 6f))

        viewModel.onContextMenuActionRemoveApp(installedApp)

        assertEquals(LauncherOverlay.None, viewModel.activeOverlay.value)
    }

    @Test
    fun `context menu app info action dismisses the menu overlay`() = runTest(testDispatcher) {
        viewModel.onAppLongPressed(installedApp, Offset(x = 5f, y = 6f))
        assertTrue(viewModel.activeOverlay.value is LauncherOverlay.AppContextMenu)

        viewModel.onContextMenuActionAppInfo(installedApp)

        assertEquals(LauncherOverlay.None, viewModel.activeOverlay.value)
    }

    @Test
    fun `context menu uninstall action keeps menu open when uninstall flow cannot start`() = runTest(testDispatcher) {
        viewModel.onAppLongPressed(installedApp, Offset(x = 5f, y = 6f))

        // A package that is not installed cannot produce a valid uninstall intent, so the
        // context menu must stay open instead of being torn down.
        viewModel.onContextMenuActionUninstallApp(removedApp)

        assertTrue(
            "Context menu must remain open when the uninstall action was not executed",
            viewModel.activeOverlay.value is LauncherOverlay.AppContextMenu
        )
    }
}
