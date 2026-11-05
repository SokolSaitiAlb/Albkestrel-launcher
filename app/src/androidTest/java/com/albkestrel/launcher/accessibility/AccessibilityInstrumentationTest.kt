package com.albkestrel.launcher.accessibility

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
import com.albkestrel.launcher.core.designsystem.FolderIcon
import com.albkestrel.launcher.core.designsystem.LivingAppIcon
import com.albkestrel.launcher.domain.model.LauncherApp
import com.albkestrel.launcher.domain.model.WorkspaceFolder
import org.junit.Rule
import org.junit.Test

/**
 * Device-side acceptance checks for the shared launcher primitives.
 *
 * These mirror the Robolectric unit assertions but run against a real Android runtime, where
 * TalkBack, font scaling, and density handling can differ from the JVM host. They compile in CI
 * even when no device is attached; physical execution is tracked in docs/DEVICE_TEST_MATRIX.md.
 */
class AccessibilityInstrumentationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val app = LauncherApp(
        id = "app_camera",
        name = "Camera",
        packageName = "com.example.camera",
        activityName = "com.example.camera.MainActivity",
        isInstalledApp = true
    )

    private val folder = WorkspaceFolder(
        id = "folder_media",
        title = "Media"
    )

    @Test
    fun appIconExposesButtonRoleAndMinimumTarget() {
        composeTestRule.setContent {
            MaterialTheme {
                LivingAppIcon(
                    app = app,
                    iconSize = 24.dp,
                    onClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag("app_icon_${app.id}")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertContentDescriptionEquals("Open Camera")
            .assertHasClickAction()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun folderIconExposesButtonRoleAndMinimumTarget() {
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
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertContentDescriptionEquals("Open folder Media")
            .assertHasClickAction()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }
}
