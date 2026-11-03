package com.albkestrel.launcher.domain.repository

import com.albkestrel.launcher.domain.contextmenu.AppShortcut
import com.albkestrel.launcher.domain.model.LauncherApp
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    /** Emits whenever the set of installed launchable packages changes. */
    val packageChanges: Flow<Unit>

    fun loadAllApps(): List<LauncherApp>
    fun launchApp(app: LauncherApp): Boolean
    fun findAppByPackage(packageName: String): LauncherApp?

    /** Opens Android's official application details screen for the given package. */
    fun openAppDetails(packageName: String): Boolean

    /** Starts the official Android package uninstall flow for the given package. */
    fun uninstallApp(packageName: String): Boolean

    /** Discovers shortcuts declared by or dynamically provided for the given package. */
    fun getShortcutsForPackage(packageName: String): List<AppShortcut>

    /** Executes a discovered shortcut by its stable shortcut id. */
    fun launchShortcut(app: LauncherApp, shortcutId: String): Boolean
}
