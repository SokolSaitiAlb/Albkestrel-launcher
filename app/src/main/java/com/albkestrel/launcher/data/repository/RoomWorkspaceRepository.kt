package com.albkestrel.launcher.data.repository

import androidx.room.withTransaction
import com.albkestrel.launcher.data.database.LauncherDatabase
import com.albkestrel.launcher.data.database.entity.FolderEntity
import com.albkestrel.launcher.data.database.entity.FolderItemEntity
import com.albkestrel.launcher.data.database.entity.WorkspaceItemEntity
import com.albkestrel.launcher.data.database.entity.WorkspaceMetadataEntity
import com.albkestrel.launcher.data.database.entity.WorkspacePageEntity
import com.albkestrel.launcher.domain.model.GridCoordinate
import com.albkestrel.launcher.domain.model.LauncherApp
import com.albkestrel.launcher.domain.model.WorkspaceFolder
import com.albkestrel.launcher.domain.model.WorkspaceItem
import com.albkestrel.launcher.domain.model.WorkspaceItemType
import com.albkestrel.launcher.domain.model.WorkspacePage
import com.albkestrel.launcher.domain.repository.AppRepository
import com.albkestrel.launcher.domain.repository.WorkspaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RoomWorkspaceRepository(
    private val database: LauncherDatabase,
    private val appRepository: AppRepository
) : WorkspaceRepository {

    private val workspaceDao = database.workspaceDao()
    private val folderDao = database.folderDao()
    private val initMutex = Mutex()

    override suspend fun initializeWorkspaceIfNeeded() {
        initMutex.withLock {
            val installedApps = withContext(Dispatchers.IO) {
                appRepository.loadAllApps().filter { it.isInstalledApp }
            }

            database.withTransaction {
                val metadataDao = database.workspaceMetadataDao()

                // The bootstrap marker is written in the same transaction as the cleanup and
                // seeding below. Once present, no legacy cleanup or reseeding may happen again.
                if (metadataDao.getValue(WorkspaceMetadataEntity.KEY_WORKSPACE_BOOTSTRAP_COMPLETE) == "1") {
                    return@withTransaction
                }

                val isFreshDatabase =
                    metadataDao.getValue(WorkspaceMetadataEntity.KEY_DATABASE_PRE_V3) != "1"

                // Pages are structural: recover them even for pre-v3 databases so the UI can
                // render. No home, dock, or folder content is created here.
                if (workspaceDao.getPageCount() == 0) {
                    workspaceDao.insertPages(
                        listOf(
                            WorkspacePageEntity(id = "page_0", pageIndex = 0),
                            WorkspacePageEntity(id = "page_1", pageIndex = 1)
                        )
                    )
                }

                val itemsBeforeCleanup = workspaceDao.getAllItems()
                val folderItemsBeforeCleanup = folderDao.getAllFolderItems()
                val hadLegacyDockItems = itemsBeforeCleanup.any { item ->
                    item.isDock && item.packageName in LEGACY_PLACEHOLDER_PACKAGES
                }
                val hadLegacyHomeApps = itemsBeforeCleanup.any { item ->
                    !item.isDock &&
                        item.itemType == "APP" &&
                        item.packageName in LEGACY_PLACEHOLDER_PACKAGES
                }
                val hadLegacyFolderItems = folderItemsBeforeCleanup.any { item ->
                    item.packageName in LEGACY_PLACEHOLDER_PACKAGES
                }

                // One-time legacy cleanup. Only the exact known placeholder packages are
                // removed; every user-added app, including future real Albkestrel apps, is
                // preserved after this transaction commits.
                workspaceDao.deleteItemsByPackageNames(LEGACY_PLACEHOLDER_PACKAGES)
                folderDao.deleteFolderItemsByPackageNames(LEGACY_PLACEHOLDER_PACKAGES)

                var existingItems = workspaceDao.getAllItems()
                val existingIds = existingItems.map { it.id }.toMutableSet()
                val occupied = existingItems
                    .map { "${it.pageId}:${it.cellX},${it.cellY}" }
                    .toMutableSet()

                // Fresh installs get the default folder shortcuts. Existing databases keep
                // exactly the shortcuts the user already has, even when the home page is empty.
                if (isFreshDatabase) {
                    if (folderDao.getFolderCount() == 0) {
                        folderDao.insertFolders(
                            listOf(
                                FolderEntity(
                                    id = "folder_creativity",
                                    title = "Creativity",
                                    accentColorArgb = 0xFF8B5CF6
                                ),
                                FolderEntity(
                                    id = "folder_essentials",
                                    title = "Essentials",
                                    accentColorArgb = 0xFF00E5FF
                                )
                            )
                        )
                    }

                    val folderReferences = listOf(
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
                        )
                    ).filter { candidate ->
                        candidate.id !in existingIds &&
                            "${candidate.pageId}:${candidate.cellX},${candidate.cellY}" !in occupied
                    }

                    if (folderReferences.isNotEmpty()) {
                        workspaceDao.insertItems(folderReferences)
                        existingItems = existingItems + folderReferences
                        folderReferences.forEach { reference ->
                            existingIds += reference.id
                            occupied += "${reference.pageId}:${reference.cellX},${reference.cellY}"
                        }
                    }
                }

                val shouldSeedDock = isFreshDatabase || hadLegacyDockItems
                val shouldSeedHome = isFreshDatabase || hadLegacyHomeApps
                val shouldSeedFolders = isFreshDatabase || hadLegacyFolderItems

                // Seed only the areas that are being created or whose legacy placeholders were
                // removed. Existing user placements always win, including an intentionally
                // empty dock or home page.
                val usedPackages = existingItems.mapNotNull { it.packageName }.toMutableSet()
                val availableApps = installedApps.filterNot { it.packageName in usedPackages }
                var appCursor = 0
                val seedItems = mutableListOf<WorkspaceItemEntity>()

                if (shouldSeedDock && existingItems.none { it.isDock }) {
                    DOCK_CELLS.forEachIndexed { index, (cellX, cellY) ->
                        val app = availableApps.getOrNull(appCursor) ?: return@forEachIndexed
                        seedItems += WorkspaceItemEntity(
                            id = "seed_dock_${app.packageName}",
                            itemType = "APP",
                            pageId = "dock",
                            cellX = cellX,
                            cellY = cellY,
                            position = index,
                            isDock = true,
                            packageName = app.packageName
                        )
                        usedPackages += app.packageName
                        appCursor++
                    }
                }

                if (shouldSeedHome && existingItems.none { !it.isDock && it.itemType == "APP" }) {
                    HOME_APP_CELLS.forEachIndexed { index, (cellX, cellY) ->
                        if ("page_0:$cellX,$cellY" in occupied) return@forEachIndexed
                        val app = availableApps.getOrNull(appCursor) ?: return@forEachIndexed
                        seedItems += WorkspaceItemEntity(
                            id = "seed_home_${app.packageName}",
                            itemType = "APP",
                            pageId = "page_0",
                            cellX = cellX,
                            cellY = cellY,
                            position = index + 2,
                            packageName = app.packageName
                        )
                        occupied += "page_0:$cellX,$cellY"
                        usedPackages += app.packageName
                        appCursor++
                    }
                }

                if (seedItems.isNotEmpty()) {
                    workspaceDao.insertItems(seedItems)
                }

                // Populate folders only for a fresh install or when legacy folder entries were
                // removed. An existing folder the user emptied is deliberately left empty.
                if (
                    shouldSeedFolders &&
                    folderDao.getAllFolderItems().isEmpty() &&
                    installedApps.isNotEmpty()
                ) {
                    val folderApps = installedApps.filterNot { it.packageName in usedPackages }
                    if (folderApps.isNotEmpty()) {
                        val chunkSize = ((folderApps.size + 1) / 2).coerceAtLeast(1)
                        val chunks = folderApps.chunked(chunkSize)
                        folderDao.getAllFolders()
                            .filter { it.id in DEFAULT_FOLDER_IDS }
                            .forEachIndexed { index, folder ->
                                val appsForFolder = chunks.getOrNull(index).orEmpty()
                                if (appsForFolder.isNotEmpty()) {
                                    folderDao.insertFolderItems(
                                        appsForFolder.mapIndexed { position, app ->
                                            FolderItemEntity(
                                                id = "seed_${folder.id}_${app.packageName}",
                                                folderId = folder.id,
                                                packageName = app.packageName,
                                                position = position
                                            )
                                        }
                                    )
                                }
                            }
                    }
                }

                metadataDao.put(
                    WorkspaceMetadataEntity(
                        WorkspaceMetadataEntity.KEY_WORKSPACE_BOOTSTRAP_COMPLETE,
                        "1"
                    )
                )
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
            folderDao.observeFolderItems(),
            appRepository.packageChanges.onStart { emit(Unit) }
        ) { folders, folderItems, _ ->
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
        }.flowOn(Dispatchers.IO)
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
                        val isInstalled = resolvedApp?.isInstalledApp == true
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
        }.flowOn(Dispatchers.IO)
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

    private companion object {
        /**
         * Exact package names that belonged to the Phase 1 mock suite. They are removed from
         * persisted layouts only, so the old prototype icons cannot survive an upgrade.
         */
        val LEGACY_PLACEHOLDER_PACKAGES = listOf(
            "com.albkestrel.dialer",
            "com.albkestrel.messaging",
            "com.albkestrel.browser",
            "com.albkestrel.camera",
            "com.albkestrel.gallery",
            "com.albkestrel.music",
            "com.albkestrel.settings",
            "com.albkestrel.clock",
            "com.albkestrel.weather",
            "com.albkestrel.calendar",
            "com.albkestrel.files",
            "com.albkestrel.notes",
            "com.albkestrel.health",
            "com.albkestrel.maps",
            "com.albkestrel.studio",
            "com.albkestrel.calc",
            "com.albkestrel.recorder",
            "com.albkestrel.gamespace"
        )

        val DEFAULT_FOLDER_IDS = setOf("folder_creativity", "folder_essentials")

        val DOCK_CELLS = listOf(
            0 to 0,
            1 to 0,
            2 to 0,
            3 to 0
        )

        val HOME_APP_CELLS = listOf(
            2 to 0,
            3 to 0,
            0 to 1,
            1 to 1,
            2 to 1,
            3 to 1
        )
    }
}
