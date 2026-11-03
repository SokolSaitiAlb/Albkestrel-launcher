package com.albkestrel.launcher

import androidx.test.core.app.ApplicationProvider
import com.albkestrel.launcher.app.AlbkestrelApplication
import com.albkestrel.launcher.domain.model.LauncherApp
import com.albkestrel.launcher.domain.model.WorkspaceFolder
import com.albkestrel.launcher.feature.common.LauncherOverlay
import com.albkestrel.launcher.feature.common.LauncherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LauncherHomeLifecycleTest {

    private lateinit var application: AlbkestrelApplication
    private lateinit var viewModel: LauncherViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        viewModel = LauncherViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `scenario 1 - HOME closes active overlay while preserving workspace page`() = runTest(testDispatcher) {
        viewModel.setHomePage(1)
        viewModel.setOverlay(LauncherOverlay.AppDrawer)
        assertEquals(LauncherOverlay.AppDrawer, viewModel.activeOverlay.value)
        assertEquals(1, viewModel.homePageIndex.value)

        val handled = viewModel.onHomeIntentReceived()

        assertTrue("HOME intent should be handled when overlay is open", handled)
        assertEquals(LauncherOverlay.None, viewModel.activeOverlay.value)
        assertEquals("Workspace page must be preserved when closing overlay", 1, viewModel.homePageIndex.value)
    }

    @Test
    fun `scenario 2 - HOME closes search overlay, resets search query, and preserves page`() = runTest(testDispatcher) {
        viewModel.setHomePage(1)
        viewModel.setOverlay(LauncherOverlay.Search)
        viewModel.setSearchQuery("Camera")
        assertEquals(LauncherOverlay.Search, viewModel.activeOverlay.value)
        assertEquals("Camera", viewModel.searchQuery.value)
        assertEquals(1, viewModel.homePageIndex.value)

        val handled = viewModel.onHomeIntentReceived()

        assertTrue(handled)
        assertEquals(LauncherOverlay.None, viewModel.activeOverlay.value)
        assertEquals("Search query must be cleared on search overlay dismissal", "", viewModel.searchQuery.value)
        assertEquals("Workspace page must be preserved on search overlay dismissal", 1, viewModel.homePageIndex.value)
    }

    @Test
    fun `scenario 3 - HOME with no overlay returns from secondary page to page 0`() = runTest(testDispatcher) {
        viewModel.closeOverlay()
        viewModel.setHomePage(1)
        assertEquals(LauncherOverlay.None, viewModel.activeOverlay.value)
        assertEquals(1, viewModel.homePageIndex.value)

        val handled = viewModel.onHomeIntentReceived()

        assertTrue("HOME intent should be handled when returning to page 0", handled)
        assertEquals(0, viewModel.homePageIndex.value)
        assertEquals(LauncherOverlay.None, viewModel.activeOverlay.value)
    }

    @Test
    fun `scenario 4 - HOME with no overlay on page 0 is a graceful no-op`() = runTest(testDispatcher) {
        viewModel.closeOverlay()
        viewModel.setHomePage(0)
        assertEquals(LauncherOverlay.None, viewModel.activeOverlay.value)
        assertEquals(0, viewModel.homePageIndex.value)

        val handled = viewModel.onHomeIntentReceived()

        assertFalse("HOME intent returns false when already at root page 0 with no overlays", handled)
        assertEquals(0, viewModel.homePageIndex.value)
        assertEquals(LauncherOverlay.None, viewModel.activeOverlay.value)
    }

    @Test
    fun `scenario 5 - folder expanded on page 1 handles 2-step HOME sequence`() = runTest(testDispatcher) {
        val testFolder = WorkspaceFolder(id = "folder_test", title = "Test Tools")
        viewModel.setHomePage(1)
        viewModel.openFolder(testFolder)
        assertTrue(viewModel.activeOverlay.value is LauncherOverlay.FolderExpanded)
        assertEquals(1, viewModel.homePageIndex.value)

        // Step 1: First HOME collapses folder modal and preserves Page 1
        val step1Handled = viewModel.onHomeIntentReceived()
        assertTrue(step1Handled)
        assertEquals(LauncherOverlay.None, viewModel.activeOverlay.value)
        assertEquals(1, viewModel.homePageIndex.value)

        // Step 2: Second HOME returns from Page 1 to Page 0
        val step2Handled = viewModel.onHomeIntentReceived()
        assertTrue(step2Handled)
        assertEquals(0, viewModel.homePageIndex.value)
    }

    @Test
    fun `unavailable app click never opens a prototype preview overlay`() = runTest(testDispatcher) {
        viewModel.closeOverlay()

        viewModel.onAppClicked(
            LauncherApp(
                id = "missing",
                name = "Missing",
                packageName = "com.missing.app",
                isInstalledApp = false
            )
        )

        assertEquals(LauncherOverlay.None, viewModel.activeOverlay.value)
    }
}
