package com.albkestrel.launcher.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import com.albkestrel.launcher.domain.model.LauncherApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Guards the accessibility contract of the shared launcher icon primitive.
 *
 * Touch input is handled by pointerInput and is not exercised here. These tests verify the
 * semantics actions that TalkBack and switch access use, so a future gesture rewrite cannot
 * silently drop click or long-click accessibility again.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LivingAppIconAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val app = LauncherApp(
        id = "app_gallery",
        name = "Gallery",
        packageName = "com.example.gallery",
        activityName = "com.example.gallery.MainActivity",
        isInstalledApp = true
    )

    @Test
    fun `icon exposes distinct click and long click semantics`() {
        var clickCount = 0
        var longPressedApp: LauncherApp? = null
        var longPressAnchor: Offset? = null

        composeTestRule.setContent {
            MaterialTheme {
                LivingAppIcon(
                    app = app,
                    iconSize = 48.dp,
                    onClick = { clickCount++ },
                    onLongPress = { pressedApp, anchor ->
                        longPressedApp = pressedApp
                        longPressAnchor = anchor
                    }
                )
            }
        }

        val icon = composeTestRule.onNodeWithTag("app_icon_${app.id}")
        icon.assertHasClickAction()
        icon.assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))

        icon.performSemanticsAction(SemanticsActions.OnClick)
        assertEquals("Click semantics must invoke the launcher click handler", 1, clickCount)

        icon.performSemanticsAction(SemanticsActions.OnLongClick)
        assertEquals("Long-click semantics must report the same app", app, longPressedApp)
        assertNotNull("Long-click semantics must provide an anchor position", longPressAnchor)
    }

    @Test
    fun `icon without long press handler does not advertise a long click action`() {
        composeTestRule.setContent {
            MaterialTheme {
                LivingAppIcon(
                    app = app,
                    iconSize = 48.dp,
                    onClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag("app_icon_${app.id}")
            .assertHasClickAction()
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnLongClick))
    }

    @Test
    fun `icon exposes a button role with a spoken app label`() {
        composeTestRule.setContent {
            MaterialTheme {
                LivingAppIcon(
                    app = app,
                    iconSize = 48.dp,
                    onClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag("app_icon_${app.id}")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertContentDescriptionEquals("Open Gallery")
    }

    @Test
    fun `icon meets the 48 dp minimum touch target`() {
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
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }
}
