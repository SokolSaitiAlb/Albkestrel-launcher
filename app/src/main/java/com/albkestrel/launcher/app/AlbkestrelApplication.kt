package com.albkestrel.launcher.app

import android.app.Application
import com.albkestrel.launcher.data.database.LauncherDatabase
import com.albkestrel.launcher.data.preferences.DataStoreSettingsRepository
import com.albkestrel.launcher.data.repository.PackageManagerAppRepository
import com.albkestrel.launcher.data.repository.RoomWorkspaceRepository
import com.albkestrel.launcher.domain.repository.AppRepository
import com.albkestrel.launcher.domain.repository.SettingsRepository
import com.albkestrel.launcher.domain.repository.WorkspaceRepository

class AlbkestrelApplication : Application() {

    val database: LauncherDatabase by lazy {
        LauncherDatabase.getInstance(this)
    }

    val appRepository: AppRepository by lazy {
        PackageManagerAppRepository(this)
    }

    val workspaceRepository: WorkspaceRepository by lazy {
        RoomWorkspaceRepository(database, appRepository)
    }

    val settingsRepository: SettingsRepository by lazy {
        DataStoreSettingsRepository(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
