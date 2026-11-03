package com.albkestrel.launcher.feature.common

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.albkestrel.launcher.app.AlbkestrelApplication
import com.albkestrel.launcher.core.common.BatteryMonitor
import com.albkestrel.launcher.core.common.BatteryStatus
import com.albkestrel.launcher.domain.model.AlbkestrelThemeMode
import com.albkestrel.launcher.domain.model.AnimationSpeed
import com.albkestrel.launcher.domain.model.AppCategory
import com.albkestrel.launcher.domain.model.IconSize
import com.albkestrel.launcher.domain.model.LauncherApp
import com.albkestrel.launcher.domain.model.LauncherSettings
import com.albkestrel.launcher.domain.model.WeatherMockData
import com.albkestrel.launcher.domain.model.WorkspaceFolder
import com.albkestrel.launcher.domain.model.WorkspaceItem
import com.albkestrel.launcher.domain.repository.AppRepository
import com.albkestrel.launcher.domain.repository.SettingsRepository
import com.albkestrel.launcher.domain.repository.WorkspaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface LauncherOverlay {
    object None : LauncherOverlay
    object AppDrawer : LauncherOverlay
    object Search : LauncherOverlay
    object Settings : LauncherOverlay
    object PrototypeInfo : LauncherOverlay
    data class FolderExpanded(val folder: WorkspaceFolder) : LauncherOverlay
}

data class LauncherUiState(
    val allApps: List<LauncherApp> = emptyList(),
    val isLoadingApps: Boolean = true,
    val workspaceItems: List<WorkspaceItem> = emptyList(),
    val folders: List<WorkspaceFolder> = emptyList(),
    val dockItems: List<WorkspaceItem> = emptyList(),
    val activeOverlay: LauncherOverlay = LauncherOverlay.None,
    val homePageIndex: Int = 0,
    val searchQuery: String = "",
    val drawerCategory: AppCategory = AppCategory.ALL,
    val weather: WeatherMockData = WeatherMockData(),
    val batteryStatus: BatteryStatus = BatteryStatus(percentage = 100, isCharging = false),
    val settings: LauncherSettings = LauncherSettings(),
    val isMediaPlaying: Boolean = true,
    val mediaTrackName: String = "Luminous Horizon",
    val mediaArtist: String = "Albkestrel Ambient Labs"
)

private data class AppListState(
    val apps: List<LauncherApp>,
    val isLoading: Boolean
)

private data class WorkspaceState(
    val items: List<WorkspaceItem>,
    val folders: List<WorkspaceFolder>,
    val overlay: LauncherOverlay,
    val pageIndex: Int
)

private data class PreferencesState(
    val query: String,
    val category: AppCategory,
    val settings: LauncherSettings,
    val isMediaPlaying: Boolean,
    val batteryStatus: BatteryStatus
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val albkestrelApp = application as AlbkestrelApplication
    val appRepository: AppRepository = albkestrelApp.appRepository
    val workspaceRepository: WorkspaceRepository = albkestrelApp.workspaceRepository
    val settingsRepository: SettingsRepository = albkestrelApp.settingsRepository

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(VibratorManager::class.java)
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Application.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _allApps = MutableStateFlow<List<LauncherApp>>(emptyList())
    private val _isLoadingApps = MutableStateFlow(true)
    private val _activeOverlay = MutableStateFlow<LauncherOverlay>(LauncherOverlay.None)
    private val _homePageIndex = MutableStateFlow(0)
    private val _searchQuery = MutableStateFlow("")
    private val _drawerCategory = MutableStateFlow(AppCategory.ALL)
    private val _isMediaPlaying = MutableStateFlow(true)
    private val _batteryStatus = MutableStateFlow(BatteryStatus(100, false))

    val activeOverlay: StateFlow<LauncherOverlay> = _activeOverlay.asStateFlow()
    val homePageIndex: StateFlow<Int> = _homePageIndex.asStateFlow()
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<LauncherUiState> = combine(
        combine(_allApps, _isLoadingApps) { apps, isLoading ->
            AppListState(apps = apps, isLoading = isLoading)
        },
        combine(
            workspaceRepository.observeWorkspaceItems(),
            workspaceRepository.observeFolders(),
            _activeOverlay,
            _homePageIndex
        ) { items, folders, overlay, page ->
            WorkspaceState(
                items = items,
                folders = folders,
                overlay = overlay,
                pageIndex = page
            )
        },
        combine(
            _searchQuery,
            _drawerCategory,
            settingsRepository.settings,
            _isMediaPlaying,
            _batteryStatus
        ) { query, category, settings, mediaPlaying, battery ->
            PreferencesState(
                query = query,
                category = category,
                settings = settings,
                isMediaPlaying = mediaPlaying,
                batteryStatus = battery
            )
        }
    ) { appState, workspaceState, preferences ->
        val dock = workspaceState.items
            .filter { it.coordinate.isDock }
            .sortedBy { it.coordinate.position }
        val pageItems = workspaceState.items.filterNot { it.coordinate.isDock }

        // If an expanded folder is active, update its reference with the latest DB folder state.
        val resolvedOverlay = if (workspaceState.overlay is LauncherOverlay.FolderExpanded) {
            val freshFolder = workspaceState.folders
                .find { it.id == workspaceState.overlay.folder.id }
                ?: workspaceState.overlay.folder
            LauncherOverlay.FolderExpanded(freshFolder)
        } else {
            workspaceState.overlay
        }

        LauncherUiState(
            allApps = appState.apps,
            isLoadingApps = appState.isLoading,
            workspaceItems = pageItems,
            folders = workspaceState.folders,
            dockItems = dock,
            activeOverlay = resolvedOverlay,
            homePageIndex = workspaceState.pageIndex,
            searchQuery = preferences.query,
            drawerCategory = preferences.category,
            settings = preferences.settings,
            batteryStatus = preferences.batteryStatus,
            isMediaPlaying = preferences.isMediaPlaying
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        LauncherUiState()
    )

    init {
        // Initialize workspace database seed if needed
        viewModelScope.launch {
            workspaceRepository.initializeWorkspaceIfNeeded()
        }

        // Refresh immediately and whenever Android reports a package change.
        viewModelScope.launch {
            appRepository.packageChanges.collect {
                loadApps()
            }
        }
        refreshApps()

        // Observe real battery status
        viewModelScope.launch {
            BatteryMonitor.observeBattery(application).collect { status ->
                _batteryStatus.value = status
            }
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            loadApps()
        }
    }

    private suspend fun loadApps() {
        if (_allApps.value.isEmpty()) {
            _isLoadingApps.value = true
        }
        try {
            _allApps.value = withContext(Dispatchers.IO) {
                appRepository.loadAllApps()
            }
        } finally {
            _isLoadingApps.value = false
        }
    }

    fun onHomeIntentReceived(): Boolean {
        triggerHapticFeedback(1)
        val currentOverlay = _activeOverlay.value
        val hadOverlay = currentOverlay !is LauncherOverlay.None
        if (hadOverlay) {
            // Dismiss active overlay while strictly preserving current homePageIndex
            if (currentOverlay is LauncherOverlay.Search) {
                _searchQuery.value = ""
            }
            _activeOverlay.value = LauncherOverlay.None
            return true
        } else {
            // No overlay: if on secondary page, return to Page 0
            if (_homePageIndex.value != 0) {
                _homePageIndex.value = 0
                return true
            }
            return false
        }
    }

    fun triggerHapticFeedback(pattern: Int = 1) {
        if (!uiState.value.settings.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (pattern) {
                    1 -> VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE)
                    2 -> VibrationEffect.createOneShot(32, VibrationEffect.DEFAULT_AMPLITUDE)
                    else -> VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    fun setOverlay(overlay: LauncherOverlay) {
        triggerHapticFeedback(1)
        _activeOverlay.value = overlay
        if (overlay !is LauncherOverlay.Search) {
            _searchQuery.value = ""
        }
    }

    fun closeOverlay() {
        triggerHapticFeedback(1)
        if (_activeOverlay.value is LauncherOverlay.Search) {
            _searchQuery.value = ""
        }
        _activeOverlay.value = LauncherOverlay.None
    }

    fun setHomePage(index: Int) {
        if (_homePageIndex.value != index) {
            triggerHapticFeedback(1)
            _homePageIndex.value = index
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDrawerCategory(category: AppCategory) {
        triggerHapticFeedback(1)
        _drawerCategory.value = category
    }

    fun openFolder(folder: WorkspaceFolder) {
        triggerHapticFeedback(1)
        _activeOverlay.value = LauncherOverlay.FolderExpanded(folder)
    }

    fun renameFolder(folderId: String, newName: String) {
        triggerHapticFeedback(2)
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            workspaceRepository.renameFolder(folderId, trimmed)
        }
    }

    fun onAppClicked(app: LauncherApp) {
        triggerHapticFeedback(2)
        val launched = appRepository.launchApp(app)
        if (launched) {
            // The real application is now in front. Returning to the launcher should land on
            // the home screen rather than leaving a stale drawer/search/folder overlay behind.
            if (_activeOverlay.value is LauncherOverlay.Search) {
                _searchQuery.value = ""
            }
            _activeOverlay.value = LauncherOverlay.None
        }
        // Apps without a launchable activity are intentionally a safe no-op. They never open the
        // old prototype preview dialog.
    }

    fun toggleMediaPlayback() {
        triggerHapticFeedback(1)
        _isMediaPlaying.value = !_isMediaPlaying.value
    }

    fun updateGridColumns(cols: Int) {
        triggerHapticFeedback(1)
        viewModelScope.launch {
            settingsRepository.updateGridColumns(cols)
        }
    }

    fun updateIconSize(size: IconSize) {
        triggerHapticFeedback(1)
        viewModelScope.launch {
            settingsRepository.updateIconSize(size)
        }
    }

    fun updateThemeMode(mode: AlbkestrelThemeMode) {
        triggerHapticFeedback(1)
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
        }
    }

    fun updateAnimationSpeed(speed: AnimationSpeed) {
        triggerHapticFeedback(1)
        viewModelScope.launch {
            settingsRepository.updateAnimationSpeed(speed)
        }
    }

    fun toggleHaptics() {
        triggerHapticFeedback(2)
        viewModelScope.launch {
            settingsRepository.toggleHaptics()
        }
    }

    fun toggleDynamicColor() {
        triggerHapticFeedback(1)
        viewModelScope.launch {
            settingsRepository.toggleDynamicColor()
        }
    }

    fun toggleDockLabels() {
        triggerHapticFeedback(1)
        viewModelScope.launch {
            settingsRepository.toggleDockLabels()
        }
    }

    fun updateWallpaperDim(dim: Float) {
        viewModelScope.launch {
            settingsRepository.updateWallpaperDim(dim)
        }
    }
}
