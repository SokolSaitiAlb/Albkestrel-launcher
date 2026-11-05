package com.albkestrel.launcher.feature.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Guards the media-control labels.
 *
 * The widget has no track-navigation callbacks yet: all three controls toggle playback. The two
 * side controls must therefore announce the play/pause action instead of claiming that they skip
 * to the previous or next track.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MediaGlanceWidgetAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `playing state labels every control with the pause action`() {
        setContent(isPlaying = true)

        composeTestRule
            .onNodeWithTag("media_previous_control")
            .assertContentDescriptionEquals("Pause")
            .assertHasClickAction()
        composeTestRule
            .onNodeWithTag("media_play_pause_control")
            .assertContentDescriptionEquals("Pause")
            .assertHasClickAction()
        composeTestRule
            .onNodeWithTag("media_next_control")
            .assertContentDescriptionEquals("Pause")
            .assertHasClickAction()
    }

    @Test
    fun `paused state labels every control with the play action`() {
        setContent(isPlaying = false)

        composeTestRule
            .onNodeWithTag("media_previous_control")
            .assertContentDescriptionEquals("Play")
        composeTestRule
            .onNodeWithTag("media_play_pause_control")
            .assertContentDescriptionEquals("Play")
        composeTestRule
            .onNodeWithTag("media_next_control")
            .assertContentDescriptionEquals("Play")
    }

    @Test
    fun `side controls preserve the existing play pause action`() {
        var toggleCount = 0
        setContent(isPlaying = true, onTogglePlay = { toggleCount++ })

        composeTestRule.onNodeWithTag("media_previous_control").performClick()
        composeTestRule.onNodeWithTag("media_next_control").performClick()

        assertEquals(
            "Side controls must keep the existing play/pause behaviour until track navigation exists",
            2,
            toggleCount
        )
    }

    private fun setContent(
        isPlaying: Boolean,
        onTogglePlay: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                MediaGlanceWidget(
                    isPlaying = isPlaying,
                    trackName = "Luminous Horizon",
                    artistName = "Albkestrel Ambient Labs",
                    onTogglePlay = onTogglePlay
                )
            }
        }
    }
}
