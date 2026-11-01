package com.albkestrel.launcher.domain.model

data class GridCoordinate(
    val pageIndex: Int,
    val cellX: Int,
    val cellY: Int,
    val spanX: Int = 1,
    val spanY: Int = 1,
    val isDock: Boolean = false,
    val position: Int = 0
)

enum class WorkspaceItemType {
    APPLICATION,
    FOLDER,
    WIDGET
}

data class WorkspaceFolder(
    val id: String,
    val title: String,
    val accentColorArgb: Long = 0xFF00E5FF,
    val items: List<LauncherApp> = emptyList()
)

data class WorkspacePage(
    val id: String,
    val pageIndex: Int
)

data class WorkspaceItem(
    val id: String,
    val coordinate: GridCoordinate,
    val itemType: WorkspaceItemType,
    val app: LauncherApp? = null,
    val folder: WorkspaceFolder? = null,
    val isInstalled: Boolean = true
)
