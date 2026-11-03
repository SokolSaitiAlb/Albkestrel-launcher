package com.albkestrel.launcher.domain.repository

import com.albkestrel.launcher.domain.model.LauncherApp
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    /** Emits whenever the set of installed launchable packages changes. */
    val packageChanges: Flow<Unit>

    fun loadAllApps(): List<LauncherApp>
    fun launchApp(app: LauncherApp): Boolean
    fun findAppByPackage(packageName: String): LauncherApp?
}
