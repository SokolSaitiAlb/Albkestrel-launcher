package com.albkestrel.launcher.data.repository

import androidx.room.withTransaction
import com.albkestrel.launcher.data.database.LauncherDatabase
import com.albkestrel.launcher.data.database.entity.FolderEntity
import com.albkestrel.launcher.data.database.entity.FolderItemEntity
import com.albkestrel.launcher.data.database.entity.WorkspaceItemEntity
import com.albkestrel.launcher.data.database.entity.WorkspacePageEntity
import com.albkestrel.launcher.domain.model.GridCoordinate
import com.albkestrel.launcher.domain.model.LauncherApp
import com.albkestrel.launcher.domain.model.WorkspaceFolder
import com.albkestrel.launcher.domain.model.WorkspaceItem
import com.albkestrel.launcher.domain.model.WorkspaceItemType
import com.albkestrel.launcher.domain.model.WorkspacePage
import com.albkestrel.launcher.domain.repository.AppRepository
import com.albkestrel.launcher.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomWorkspaceRepository(
    private val database: LauncherDatabase,
    private val appRepository: AppRepository
) : WorkspaceRepository {

    private val workspaceDao = database.workspaceDao()
    private val folderDao = database.folderDao()
    private val initMutex = Mutex()

    override suspend fun initializeWorkspaceIfNeeded() {
        initMutex.withLock {
            database.withTransaction {
                val pageCount = workspaceDao.getPageCount()
                val itemCount = workspaceDao.getItemCount()
                val folderCount = folderDao.getFolderCount()

                if (pageCount >= 2 && itemCount > 0 && folderCount > 0) {
                    // Fully initialized; preserve existing user data strictly
                    return@withTransaction
                }

                // 1. Recover/Seed Pages if missing
                if (pageCount == 0) {
                    val initialPages = listOf(
                        WorkspacePageEntity(id = "page_0", pageIndex = 0),
                        WorkspacePageEntity(id = "page_1", pageIndex = 1)
                    )
                    workspaceDao.insertPages(initialPages)
                }

                // 2. Recover/Seed Folders if missing
                if (folderCount == 0) {
                    val initialFolders = listOf(
                        FolderEntity(id = "folder_creativity", title = "Creativity", accentColorArgb = 0xFF8B5CF6),
                        FolderEntity(id = "folder_essentials", title = "Essentials", accentColorArgb = 0xFF00E5FF)
                    )
                    folderDao.insertFolders(initialFolders)

                    val creativityItems = listOf(
                        FolderItemEntity(id = "fi_cam", folderId = "folder_creativity", packageName = "com.albkestrel.camera", position = 0),
                        FolderItemEntity(id = "fi_pho", folderId = "folder_creativity", packageName = "com.albkestrel.gallery", position = 1),
                        FolderItemEntity(id = "fi_mus", folderId = "folder_creativity", packageName = "com.albkestrel.music", position = 2),
                        FolderItemEntity(id = "fi_rec", folderId = "folder_creativity", packageName = "com.albkestrel.recorder", position = 3),
                        FolderItemEntity(id = "fi_stu", folderId = "folder_creativity", packageName = "com.albkestrel.studio", position = 4)
                    )
                    val essentialsItems = listOf(
                        FolderItemEntity(id = "fi_cal", folderId = "folder_essentials", packageName = "com.albkestrel.calc", position = 0),
                        FolderItemEntity(id = "fi_fil", folderId = "folder_essentials", packageName = "com.albkestrel.files", position = 1),
                        FolderItemEntity(id = "fi_not", folderId = "folder_essentials", packageName = "com.albkestrel.notes", position = 2),
                        FolderItemEntity(id = "fi_clo", folderId = "folder_essentials", packageName = "com.albkestrel.clock", position = 3),
                        FolderItemEntity(id = "fi_map", folderId = "folder_essentials", packageName = "com.albkestrel.maps", position = 4)
                    )
                    folderDao.insertFolderItems(creativityItems + essentialsItems)
                }

                // 3. Recover/Seed Workspace & Dock Items if missing
                val existingItems = workspaceDao.getAllItems()
                val existingIds = existingItems.map { it.id }.toSet()
                val existingOccupied = existingItems.map { "${it.pageId}:${it.cellX},${it.cellY}" }.toSet()

                val defaultItems = listOf(
                    // Page 0 Items
                    WorkspaceItemEntity(
                        id = "item_f_creativity",
                        itemType = "FOLDER_REF",
                        pageId = "page_0",
                        cellX = 0,
                        cellY = 0,
                        position = 0,
                        folderId = "folder_creativity"
                    ),
                    WorkspaceItemEntity(
                        id = "item_f_essentials",
                        itemType = "FOLDER_REF",
                        pageId = "page_0",
                        cellX = 1,
                        cellY = 0,
                        position = 1,
                        folderId = "folder_essentials"
                    ),
                    WorkspaceItemEntity(
                        id = "item_app_weather",
                        itemType = "APP",
                        pageId = "page_0",
                        cellX = 2,
                        cellY = 0,
                        position = 2,
                        packageName = "com.albkestrel.weather"
                    ),
                    WorkspaceItemEntity(
                        id = "item_app_calendar",
                        itemType = "APP",
                        pageId = "page_0",
                        cellX = 3,
                        cellY = 0,
                        position = 3,
                        packageName = "com.albkestrel.calendar"
                    ),
                    WorkspaceItemEntity(
                        id = "item_app_settings",
                        itemType = "APP",
                        pageId = "page_0",
                        cellX = 0,
                        cellY = 1,
                        position = 4,
                        packageName = "com.albkestrel.settings"
                    ),
                    WorkspaceItemEntity(
                        id = "item_app_vitality",
                        itemType = "APP",
                        pageId = "page_0",
                        cellX = 1,
                        cellY = 1,
                        position = 5,
                        packageName = "com.albkestrel.health"
                    ),
                    WorkspaceItemEntity(
                        id = "item_app_games",
                        itemType = "APP",
                        pageId = "page_0",
                        cellX = 2,
                        cellY = 1,
                        position = 6,
                        packageName = "com.albkestrel.gamespace"
                    ),
                    // Dock Items
                    WorkspaceItemEntity(
                        id = "dock_phone",
                        itemType = "APP",
                        pageId = "dock",
                        cellX = 0,
                        cellY = 0,
                        position = 0,
                        isDock = true,
                        packageName = "com.albkestrel.dialer"
                    ),
                    WorkspaceItemEntity(
                        id = "dock_messages",
                        itemType = "APP",
                        pageId = "dock",
                        cellX = 1,
                        cellY = 0,
                        position = 1,
                        isDock = true,
                        packageName = "com.albkestrel.messaging"
                    ),
                    WorkspaceItemEntity(
                        id = "dock_browser",
                        itemType = "APP",
                        pageId = "dock",
                        cellX = 2,
                        cellY = 0,
                        position = 2,
                        isDock = true,
                        packageName = "com.albkestrel.browser"
                    ),
                    WorkspaceItemEntity(
                        id = "dock_camera",
                        itemType = "APP",
                        pageId = "dock",
                        cellX = 3,
                        cellY = 0,
                        position = 3,
                        isDock = true,
                        packageName = "com.albkestrel.camera"
                    )
                )

                val itemsToInsert = defaultItems.filter { candidate ->
                    candidate.id !in existingIds &&
                    "${candidate.pageId}:${candidate.cellX},${candidate.cellY}" !in existingOccupied
                }

                if (itemsToInsert.isNotEmpty()) {
                    workspaceDao.insertItems(itemsToInsert)
                }
            }
        }
    }

    override fun observePages(): Flow<List<WorkspacePage>> {
        return workspaceDao.observePages().map { list ->
            list.map { WorkspacePage(id = it.id, pageIndex = it.pageIndex) }
        }
    }

    override fun observeFolders(): Flow<List<WorkspaceFolder>> {
        return combine(
            folderDao.observeFolders(),
            folderDao.observeFolderItems()
        ) { folders, folderItems ->
            val allApps = appRepository.loadAllApps().associateBy { it.packageName }
            folders.map { folderEntity ->
                val itemsForFolder = folderItems
                    .filter { it.folderId == folderEntity.id }
                    .sortedBy { it.position }
                    .map { fi ->
                        allApps[fi.packageName] ?: LauncherApp(
                            id = "missing_${fi.packageName}",
                            name = fi.packageName.substringAfterLast('.'),
                            packageName = fi.packageName,
                            isInstalledApp = false
                        )
                    }
                WorkspaceFolder(
                    id = folderEntity.id,
                    title = folderEntity.title,
                    accentColorArgb = folderEntity.accentColorArgb,
                    items = itemsForFolder
                )
            }
        }
    }

    override fun observeWorkspaceItems(): Flow<List<WorkspaceItem>> {
        return combine(
            workspaceDao.observeItems(),
            observeFolders()
        ) { itemEntities, domainFolders ->
            val allApps = appRepository.loadAllApps().associateBy { it.packageName }
            val foldersById = domainFolders.associateBy { it.id }

            itemEntities.map { entity ->
                val coordinate = GridCoordinate(
                    pageIndex = if (entity.isDock) -1 else if (entity.pageId == "page_1") 1 else 0,
                    cellX = entity.cellX,
                    cellY = entity.cellY,
                    spanX = entity.spanX,
                    spanY = entity.spanY,
                    isDock = entity.isDock,
                    position = entity.position
                )

                when (entity.itemType) {
                    "FOLDER_REF" -> {
                        val folder = foldersById[entity.folderId]
                        WorkspaceItem(
                            id = entity.id,
                            coordinate = coordinate,
                            itemType = WorkspaceItemType.FOLDER,
                            folder = folder
                        )
                    }
                    "WIDGET_SLOT" -> {
                        WorkspaceItem(
                            id = entity.id,
                            coordinate = coordinate,
                            itemType = WorkspaceItemType.WIDGET
                        )
                    }
                    else -> {
                        val resolvedApp = entity.packageName?.let { allApps[it] }
                        val isInstalled = resolvedApp?.isInstalledApp ?: true
                        val app = resolvedApp ?: entity.packageName?.let { pkg ->
                            LauncherApp(
                                id = "fallback_$pkg",
                                name = pkg.substringAfterLast('.'),
                                packageName = pkg,
                                isInstalledApp = false
                            )
                        }
                        WorkspaceItem(
                            id = entity.id,
                            coordinate = coordinate,
                            itemType = WorkspaceItemType.APPLICATION,
                            app = app,
                            isInstalled = isInstalled
                        )
                    }
                }
            }
        }
    }

    override suspend fun renameFolder(folderId: String, newTitle: String) {
        folderDao.renameFolder(folderId, newTitle)
    }

    override suspend fun moveItem(itemId: String, newCoord: GridCoordinate) {
        val allItems = workspaceDao.getAllItems()
        val existing = allItems.find { it.id == itemId } ?: return
        val targetPageId = if (newCoord.isDock) "dock" else "page_${newCoord.pageIndex}"

        // Collision check: prevent overlapping another item on the target page/dock container
        val isColliding = allItems.any { other ->
            if (other.id == itemId) return@any false
            if (other.pageId != targetPageId) return@any false
            val otherRight = other.cellX + other.spanX
            val otherBottom = other.cellY + other.spanY
            val newRight = newCoord.cellX + newCoord.spanX
            val newBottom = newCoord.cellY + newCoord.spanY
            newCoord.cellX < otherRight && newRight > other.cellX &&
            newCoord.cellY < otherBottom && newBottom > other.cellY
        }
        if (isColliding) {
            return
        }

        val updated = existing.copy(
            pageId = targetPageId,
            cellX = newCoord.cellX,
            cellY = newCoord.cellY,
            spanX = newCoord.spanX,
            spanY = newCoord.spanY,
            position = newCoord.position,
            isDock = newCoord.isDock
        )
        workspaceDao.updateItem(updated)
    }

    override suspend fun addItem(item: WorkspaceItem) {
        val targetPageId = if (item.coordinate.isDock) "dock" else "page_${item.coordinate.pageIndex}"
        val allItems = workspaceDao.getAllItems()

        // 1. Check duplicate ID
        if (allItems.any { it.id == item.id }) {
            return
        }

        // 2. Collision check: ensure destination cells in targetPageId are free
        val isColliding = allItems.any { existing ->
            if (existing.pageId != targetPageId) return@any false
            val existingRight = existing.cellX + existing.spanX
            val existingBottom = existing.cellY + existing.spanY
            val newRight = item.coordinate.cellX + item.coordinate.spanX
            val newBottom = item.coordinate.cellY + item.coordinate.spanY
            item.coordinate.cellX < existingRight && newRight > existing.cellX &&
            item.coordinate.cellY < existingBottom && newBottom > existing.cellY
        }
        if (isColliding) {
            return
        }

        val entity = WorkspaceItemEntity(
            id = item.id,
            itemType = when (item.itemType) {
                WorkspaceItemType.APPLICATION -> "APP"
                WorkspaceItemType.FOLDER -> "FOLDER_REF"
                WorkspaceItemType.WIDGET -> "WIDGET_SLOT"
            },
            pageId = targetPageId,
            cellX = item.coordinate.cellX,
            cellY = item.coordinate.cellY,
            spanX = item.coordinate.spanX,
            spanY = item.coordinate.spanY,
            position = item.coordinate.position,
            isDock = item.coordinate.isDock,
            packageName = item.app?.packageName,
            folderId = item.folder?.id
        )
        workspaceDao.insertItem(entity)
    }

    override suspend fun removeItem(itemId: String) {
        database.withTransaction {
            val item = workspaceDao.getAllItems().find { it.id == itemId }
            if (item != null) {
                if (item.folderId != null) {
                    val otherRefs = workspaceDao.getAllItems().filter { it.id != itemId && it.folderId == item.folderId }
                    if (otherRefs.isEmpty()) {
                        folderDao.deleteFolderItemsByFolderId(item.folderId)
                        folderDao.deleteFolder(item.folderId)
                    }
                }
                workspaceDao.deleteItemById(itemId)
            }
        }
    }

    override suspend fun deleteFolder(folderId: String) {
        database.withTransaction {
            workspaceDao.deleteItemsByFolderId(folderId)
            folderDao.deleteFolderItemsByFolderId(folderId)
            folderDao.deleteFolder(folderId)
        }
    }

    override suspend fun addAppToFolder(folderId: String, packageName: String): Boolean {
        return database.withTransaction {
            val exists = folderDao.countFolderItem(folderId, packageName) > 0
            if (exists) {
                return@withTransaction false
            }
            val currentItems = folderDao.getFolderItems(folderId)
            val nextPos = currentItems.size
            val newItem = FolderItemEntity(
                id = "fi_${folderId}_${packageName.hashCode().toUInt()}",
                folderId = folderId,
                packageName = packageName,
                position = nextPos
            )
            folderDao.insertFolderItem(newItem)
            true
        }
    }
}
