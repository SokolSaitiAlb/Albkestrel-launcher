package com.albkestrel.launcher.domain.repository

import com.albkestrel.launcher.domain.model.LauncherApp

interface AppRepository {
    fun loadAllApps(): List<LauncherApp>
    fun launchApp(app: LauncherApp): Boolean
    fun findAppByPackage(packageName: String): LauncherApp?
}
