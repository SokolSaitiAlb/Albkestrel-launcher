package com.albkestrel.launcher.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.albkestrel.launcher.domain.model.WorkspaceFolder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Guards the accessibility contract of the folder icon, which is the second shared launcher
 * primitive used on the home grid.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FolderIconAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val folder = WorkspaceFolder(
        id = "folder_tools",
        title = "Tools"
    )

    @Test
    fun `folder icon exposes a button role and click action`() {
        composeTestRule.setContent {
            MaterialTheme {
                FolderIcon(
                    folder = folder,
                    iconSize = 48.dp,
                    onClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag("folder_icon_${folder.id}")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertContentDescriptionEquals("Open folder Tools")
            .assertHasClickAction()
    }

    @Test
    fun `folder icon meets the 48 dp minimum touch target`() {
        composeTestRule.setContent {
            MaterialTheme {
                FolderIcon(
                    folder = folder,
                    iconSize = 24.dp,
                    onClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag("folder_icon_${folder.id}")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }
}
