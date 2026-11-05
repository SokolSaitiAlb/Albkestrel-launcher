package com.albkestrel.launcher.feature.folders

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import com.albkestrel.launcher.app.AlbkestrelApplication
import com.albkestrel.launcher.domain.model.WorkspaceFolder
import com.albkestrel.launcher.feature.common.LauncherUiState
import com.albkestrel.launcher.feature.common.LauncherViewModel
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Guards the accessibility contract of the folder title control.
 *
 * The visible folder name must be part of the accessible description so a TalkBack user can tell
 * which folder's title they are editing when more than one folder exists.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FolderModalScreenAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: LauncherViewModel

    private val folder = WorkspaceFolder(
        id = "folder_tools",
        title = "Tools"
    )

    @Before
    fun setUp() {
        val application = ApplicationProvider.getApplicationContext<AlbkestrelApplication>()
        viewModel = LauncherViewModel(application)
    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun `folder title control announces the folder identity while remaining an edit button`() {
        composeTestRule.setContent {
            MaterialTheme {
                FolderModalScreen(
                    folder = folder,
                    uiState = LauncherUiState(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule
            .onNodeWithTag("folder_title_display")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertContentDescriptionEquals("Edit folder title, Tools")
            .assertHasClickAction()
    }
}
