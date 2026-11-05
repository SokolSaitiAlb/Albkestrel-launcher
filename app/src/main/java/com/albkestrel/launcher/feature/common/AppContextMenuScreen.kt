package com.albkestrel.launcher.feature.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.albkestrel.launcher.domain.contextmenu.AppContextMenuPolicy
import com.albkestrel.launcher.domain.model.LauncherApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Anchored long-press context menu for installed application icons.
 *
 * The menu is rendered through a popup anchored near the icon that triggered the long press. It
 * supports dismissal via outside tap, back press and action selection, and never emits actions for
 * apps that are no longer installed.
 */
@Composable
fun AppContextMenuScreen(
    app: LauncherApp,
    anchorPosition: Offset,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.dismissAppContextMenu()
    }

    // Shortcut discovery touches PackageManager/LauncherApps, so resolve it off the main thread
    // once per app rather than during composition while the menu is visible.
    val actions by produceState(
        initialValue = AppContextMenuPolicy.actionsFor(app, emptyList()),
        app.id,
        app.packageName,
        app.isInstalledApp
    ) {
        value = withContext(Dispatchers.IO) {
            val shortcuts = if (app.isInstalledApp) {
                viewModel.appRepository.getShortcutsForPackage(app.packageName)
            } else {
                emptyList()
            }
            AppContextMenuPolicy.actionsFor(app, shortcuts)
        }
    }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val menuWidthDp = 250.dp
    val menuWidthPx = with(density) { menuWidthDp.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val marginPx = with(density) { 16.dp.toPx() }
    val maxMenuHeightDp = (configuration.screenHeightDp - 32).coerceAtLeast(120).dp
    val maxMenuHeightPx = with(density) { maxMenuHeightDp.toPx() }
    val estimatedHeightPx = with(density) { (actions.size * 48 + 24).dp.toPx() }
    val effectiveHeightPx = minOf(estimatedHeightPx, maxMenuHeightPx)

    val clampedX = if (anchorPosition.x + menuWidthPx > screenWidthPx - marginPx) {
        (screenWidthPx - menuWidthPx - marginPx).coerceAtLeast(marginPx)
    } else {
        anchorPosition.x.coerceAtLeast(marginPx)
    }

    val clampedY = if (anchorPosition.y + effectiveHeightPx > screenHeightPx - marginPx) {
        (anchorPosition.y - effectiveHeightPx).coerceAtLeast(marginPx)
    } else {
        anchorPosition.y.coerceAtLeast(marginPx)
    }

    val anchorOffset = IntOffset(clampedX.toInt(), clampedY.toInt())

    Popup(
        alignment = Alignment.TopStart,
        offset = anchorOffset,
        onDismissRequest = { viewModel.dismissAppContextMenu() },
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .testTag("app_context_menu")
                .width(menuWidthDp)
                .heightIn(max = maxMenuHeightDp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    RoundedCornerShape(20.dp)
                )
                .clip(RoundedCornerShape(20.dp))
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            actions.forEach { action ->
                DropdownMenuItem(
                    modifier = Modifier.testTag("context_menu_action_${action.id}"),
                    text = {
                        Text(
                            text = action.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        when (action.id) {
                            AppContextMenuPolicy.ACTION_APP_INFO -> viewModel.onContextMenuActionAppInfo(app)
                            AppContextMenuPolicy.ACTION_PAUSE_APP -> viewModel.onContextMenuActionPauseApp(app)
                            AppContextMenuPolicy.ACTION_WIDGETS -> viewModel.onContextMenuActionWidgets(app)
                            AppContextMenuPolicy.ACTION_REMOVE -> viewModel.onContextMenuActionRemoveApp(app)
                            AppContextMenuPolicy.ACTION_UNINSTALL -> viewModel.onContextMenuActionUninstallApp(app)
                            else -> {
                                val shortcutId = action.shortcutId
                                if (shortcutId != null) {
                                    viewModel.onContextMenuActionLaunchShortcut(app, shortcutId)
                                }
                            }
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when {
                                action.id == AppContextMenuPolicy.ACTION_APP_INFO -> Icons.Rounded.Info
                                action.id == AppContextMenuPolicy.ACTION_REMOVE -> Icons.Rounded.Delete
                                action.id == AppContextMenuPolicy.ACTION_UNINSTALL -> Icons.AutoMirrored.Rounded.ExitToApp
                                action.isShortcutAction -> Icons.Rounded.Star
                                else -> Icons.Rounded.Info
                            },
                            contentDescription = action.title,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }
}
