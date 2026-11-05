package com.albkestrel.launcher.domain.contextmenu

import com.albkestrel.launcher.domain.model.LauncherApp

/**
 * Canonical identifiers for the actions exposed by the long-press app context menu.
 */
object AppContextMenuPolicy {

    const val ACTION_APP_INFO = "app_info"
    const val ACTION_PAUSE_APP = "pause_app"
    const val ACTION_WIDGETS = "widgets"
    const val ACTION_REMOVE = "remove_from_launcher"
    const val ACTION_UNINSTALL = "uninstall"
    const val ACTION_SHORTCUT = "shortcut"

    /**
     * Whether the OS-level pause capability is usable by the launcher today.
     *
     * Android's Digital Wellbeing does not expose a public per-app pause API that a launcher
     * process can call. Until such an integration exists, the context menu never surfaces the
     * pause action and the caller handles the unavailable case gracefully.
     */
    fun isPauseSupported(): Boolean = false

    /**
     * Whether the launcher currently hosts third-party app widgets.
     *
     * No real [android.appwidget.AppWidgetHost] implementation exists yet, so the context menu
     * cannot present a genuine widget picker. The action stays unavailable until the future
     * integration point lands; a fake picker must not be rendered.
     */
    fun isWidgetSupportAvailable(): Boolean = false

    /**
     * System apps cannot be uninstalled through a launcher-initiated flow.
     */
    fun isUninstallBlocked(app: LauncherApp): Boolean = app.isSystemApp

    /**
     * Builds the ordered list of context menu actions that are currently applicable to [app].
     *
     * - Missing / uninstalled apps yield an empty list.
     * - Actions whose underlying Android integration is unavailable are omitted.
     * - Shortcut actions are only emitted for shortcuts that were actually discovered for the app.
     */
    fun actionsFor(
        app: LauncherApp,
        shortcuts: List<AppShortcut> = emptyList()
    ): List<AppContextMenuAction> {
        if (!app.isInstalledApp) {
            return emptyList()
        }

        val actions = mutableListOf<AppContextMenuAction>()
        actions += AppContextMenuAction(
            id = AppContextMenuPolicy.ACTION_APP_INFO,
            title = "App info"
        )

        val appShortcuts = shortcuts.filter { it.packageName == app.packageName }
        appShortcuts.forEach { shortcut ->
            actions += AppContextMenuAction(
                id = "${AppContextMenuPolicy.ACTION_SHORTCUT}:${shortcut.id}",
                title = shortcut.shortLabel.ifBlank { app.name }
            )
        }

        // Reserved integration points. These branches stay dormant while the corresponding
        // Android capability is unavailable and light up automatically once it is implemented.
        if (isPauseSupported()) {
            actions += AppContextMenuAction(
                id = AppContextMenuPolicy.ACTION_PAUSE_APP,
                title = "Pause app"
            )
        }
        if (isWidgetSupportAvailable()) {
            actions += AppContextMenuAction(
                id = AppContextMenuPolicy.ACTION_WIDGETS,
                title = "Widgets"
            )
        }

        actions += AppContextMenuAction(
            id = AppContextMenuPolicy.ACTION_REMOVE,
            title = "Remove from launcher"
        )

        if (!isUninstallBlocked(app)) {
            actions += AppContextMenuAction(
                id = AppContextMenuPolicy.ACTION_UNINSTALL,
                title = "Uninstall"
            )
        }

        return actions
    }
}

/**
 * A single actionable row rendered inside the long-press context menu.
 */
data class AppContextMenuAction(
    val id: String,
    val title: String
) {
    val isShortcutAction: Boolean
        get() = id.startsWith("${AppContextMenuPolicy.ACTION_SHORTCUT}:")

    val shortcutId: String?
        get() = if (isShortcutAction) {
            id.substringAfter("${AppContextMenuPolicy.ACTION_SHORTCUT}:", "")
        } else {
            null
        }
}

/**
 * Domain-level app shortcut discovered through Android's supported shortcut APIs.
 */
data class AppShortcut(
    val id: String,
    val packageName: String,
    val activityName: String?,
    val shortLabel: String,
    val longLabel: String,
    val isDynamic: Boolean = false,
    val isDeclaredInManifest: Boolean = false
)
