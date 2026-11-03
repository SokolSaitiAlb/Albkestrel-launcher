package com.albkestrel.launcher

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.albkestrel.launcher.data.database.LauncherDatabase
import com.albkestrel.launcher.data.database.entity.FolderEntity
import com.albkestrel.launcher.data.database.entity.FolderItemEntity
import com.albkestrel.launcher.data.database.entity.WorkspaceItemEntity
import com.albkestrel.launcher.data.database.entity.WorkspaceMetadataEntity
import com.albkestrel.launcher.data.database.entity.WorkspacePageEntity
import com.albkestrel.launcher.data.repository.RoomWorkspaceRepository
import com.albkestrel.launcher.domain.contextmenu.AppShortcut
import com.albkestrel.launcher.domain.model.GridCoordinate
import com.albkestrel.launcher.domain.model.LauncherApp
import com.albkestrel.launcher.domain.model.WorkspaceItem
import com.albkestrel.launcher.domain.model.WorkspaceItemType
import com.albkestrel.launcher.domain.repository.AppRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WorkspaceRepositoryTest {

    private lateinit var database: LauncherDatabase
    private lateinit var appRepository: AppRepository
    private lateinit var workspaceRepository: RoomWorkspaceRepository

    private val testApps = listOf(
        testApp("Alpha", "com.test.alpha"),
        testApp("Bravo", "com.test.bravo"),
        testApp("Charlie", "com.test.charlie"),
        testApp("Delta", "com.test.delta"),
        testApp("Echo", "com.test.echo"),
        testApp("Foxtrot", "com.test.foxtrot"),
        testApp("Golf", "com.test.golf"),
        testApp("Hotel", "com.test.hotel"),
        testApp("India", "com.test.india"),
        testApp("Juliet", "com.test.juliet"),
        testApp("Kilo", "com.test.kilo"),
        testApp("Lima", "com.test.lima"),
        testApp("Mike", "com.test.mike"),
        testApp("November", "com.test.november"),
        testApp("Oscar", "com.test.oscar"),
        testApp("Papa", "com.test.papa"),
        testApp("Quebec", "com.test.quebec"),
        testApp("Romeo", "com.test.romeo"),
        testApp("Sierra", "com.test.sierra"),
        testApp("Tango", "com.test.tango")
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            LauncherDatabase::class.java
        ).allowMainThreadQueries().build()

        appRepository = FakeAppRepository(testApps)
        workspaceRepository = RoomWorkspaceRepository(database, appRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `empty workspace initializes with pages, dock, and default items`() = runBlocking {
        // Initial state
        assertEquals(0, database.workspaceDao().getPageCount())

        // Execute initialization
        workspaceRepository.initializeWorkspaceIfNeeded()

        // Verify pages
        val pages = workspaceRepository.observePages().first()
        assertEquals(2, pages.size)
        assertEquals(0, pages[0].pageIndex)
        assertEquals(1, pages[1].pageIndex)

        // Verify items
        val items = workspaceRepository.observeWorkspaceItems().first()
        assertTrue(items.isNotEmpty())

        val dockItems = items.filter { it.coordinate.isDock }
        assertEquals(4, dockItems.size)

        val page0Items = items.filter { !it.coordinate.isDock }
        assertTrue(page0Items.any { it.itemType == WorkspaceItemType.FOLDER })
        assertTrue(page0Items.any { it.itemType == WorkspaceItemType.APPLICATION })
    }

    @Test
    fun `existing workspace is preserved and not overwritten on subsequent initialization`() = runBlocking {
        // First initialization
        workspaceRepository.initializeWorkspaceIfNeeded()

        // User customizes a folder title
        workspaceRepository.renameFolder("folder_creativity", "Studio Suite")

        val foldersAfterRename = workspaceRepository.observeFolders().first()
        val renamed = foldersAfterRename.find { it.id == "folder_creativity" }
        assertNotNull(renamed)
        assertEquals("Studio Suite", renamed?.title)

        // Second initialization attempt (e.g. app restart)
        workspaceRepository.initializeWorkspaceIfNeeded()

        // Verify user modification was preserved
        val foldersAfterSecondInit = workspaceRepository.observeFolders().first()
        val preserved = foldersAfterSecondInit.find { it.id == "folder_creativity" }
        assertEquals("Studio Suite", preserved?.title)
    }

    @Test
    fun `pre-v3 empty workspace is not reseeded on initialization`() = runBlocking {
        database.workspaceMetadataDao().put(
            WorkspaceMetadataEntity(WorkspaceMetadataEntity.KEY_DATABASE_PRE_V3, "1")
        )
        database.workspaceDao().insertPages(
            listOf(
                WorkspacePageEntity("page_0", 0),
                WorkspacePageEntity("page_1", 1)
            )
        )
        database.folderDao().insertFolders(
            listOf(
                FolderEntity("folder_creativity", "Creativity", 0xFF8B5CF6),
                FolderEntity("folder_essentials", "Essentials", 0xFF00E5FF)
            )
        )

        workspaceRepository.initializeWorkspaceIfNeeded()
        assertEquals(0, database.workspaceDao().getItemCount())
        assertTrue(database.folderDao().getAllFolderItems().isEmpty())

        val restartRepository = RoomWorkspaceRepository(database, appRepository)
        restartRepository.initializeWorkspaceIfNeeded()

        assertEquals(0, database.workspaceDao().getItemCount())
        assertTrue(database.folderDao().getAllFolderItems().isEmpty())
    }

    @Test
    fun `intentionally cleared workspace remains empty after restart`() = runBlocking {
        workspaceRepository.initializeWorkspaceIfNeeded()
        database.workspaceDao().getAllItems().forEach { database.workspaceDao().deleteItemById(it.id) }
        database.folderDao().getAllFolderItems().forEach { database.folderDao().deleteFolderItem(it.id) }

        val restartRepository = RoomWorkspaceRepository(database, appRepository)
        restartRepository.initializeWorkspaceIfNeeded()

        assertEquals(0, database.workspaceDao().getItemCount())
        assertTrue(database.folderDao().getAllFolderItems().isEmpty())
    }

    @Test
    fun `legacy cleanup runs once and preserves later placements using placeholder package names`() = runBlocking {
        database.workspaceMetadataDao().put(
            WorkspaceMetadataEntity(WorkspaceMetadataEntity.KEY_DATABASE_PRE_V3, "1")
        )
        database.workspaceDao().insertPages(
            listOf(
                WorkspacePageEntity("page_0", 0),
                WorkspacePageEntity("page_1", 1)
            )
        )
        database.folderDao().insertFolders(
            listOf(
                FolderEntity("folder_creativity", "Creativity", 0xFF8B5CF6),
                FolderEntity("folder_essentials", "Essentials", 0xFF00E5FF)
            )
        )
        database.folderDao().insertFolderItems(
            listOf(
                FolderItemEntity(
                    id = "fi_legacy_cleanup",
                    folderId = "folder_creativity",
                    packageName = "com.albkestrel.camera",
                    position = 0
                )
            )
        )
        database.workspaceDao().insertItems(
            listOf(
                WorkspaceItemEntity(
                    id = "old_dock_cleanup",
                    itemType = "APP",
                    pageId = "dock",
                    cellX = 0,
                    cellY = 0,
                    position = 0,
                    isDock = true,
                    packageName = "com.albkestrel.dialer"
                ),
                WorkspaceItemEntity(
                    id = "old_home_cleanup",
                    itemType = "APP",
                    pageId = "page_0",
                    cellX = 2,
                    cellY = 0,
                    position = 2,
                    packageName = "com.albkestrel.weather"
                )
            )
        )

        workspaceRepository.initializeWorkspaceIfNeeded()
        assertTrue(
            "First initialization must remove legacy placeholder workspace items",
            database.workspaceDao().getAllItems().none { it.packageName == "com.albkestrel.dialer" }
        )
        assertTrue(
            "First initialization must remove legacy placeholder folder items",
            database.folderDao().getAllFolderItems().none { it.packageName == "com.albkestrel.camera" }
        )

        database.workspaceDao().insertItem(
            WorkspaceItemEntity(
                id = "future_real_camera",
                itemType = "APP",
                pageId = "page_0",
                cellX = 2,
                cellY = 0,
                position = 2,
                packageName = "com.albkestrel.camera"
            )
        )
        database.folderDao().insertFolderItem(
            FolderItemEntity(
                id = "fi_future_real_camera",
                folderId = "folder_creativity",
                packageName = "com.albkestrel.camera",
                position = 1
            )
        )

        workspaceRepository.initializeWorkspaceIfNeeded()

        assertNotNull(
            database.workspaceDao().getAllItems().find { it.id == "future_real_camera" }
        )
        assertTrue(
            database.folderDao().getAllFolderItems()
                .any { it.packageName == "com.albkestrel.camera" }
        )
    }

    @Test
    fun `workspace reloads correctly when repository is recreated`() = runBlocking {
        workspaceRepository.initializeWorkspaceIfNeeded()

        // Simulate new repository instance reading from same database
        val newRepoInstance = RoomWorkspaceRepository(database, appRepository)
        val items = newRepoInstance.observeWorkspaceItems().first()
        assertTrue(items.isNotEmpty())

        val folders = newRepoInstance.observeFolders().first()
        assertEquals(2, folders.size)
        assertTrue(folders.any { it.id == "folder_creativity" && it.items.isNotEmpty() })
    }

    @Test
    fun `concurrent initialization is idempotent and race-free`() = runBlocking {
        // Run multiple concurrent calls
        val jobs = (1..5).map {
            async {
                workspaceRepository.initializeWorkspaceIfNeeded()
            }
        }
        jobs.awaitAll()

        // Verify exactly 2 pages exist
        assertEquals(2, database.workspaceDao().getPageCount())

        // Verify items are not duplicated
        val allItems = database.workspaceDao().getAllItems()
        val ids = allItems.map { it.id }
        assertEquals(ids.size, ids.toSet().size)

        val dockItems = allItems.filter { it.isDock }
        assertEquals(4, dockItems.size)
    }

    @Test
    fun `partial initialization recovers missing components safely without data loss`() = runBlocking {
        // Simulate a scenario where only pages were inserted
        database.workspaceDao().insertPages(
            listOf(
                WorkspacePageEntity("page_0", 0),
                WorkspacePageEntity("page_1", 1)
            )
        )
        assertEquals(2, database.workspaceDao().getPageCount())
        assertEquals(0, database.workspaceDao().getItemCount())
        assertEquals(0, database.folderDao().getFolderCount())

        // Run initializeWorkspaceIfNeeded
        workspaceRepository.initializeWorkspaceIfNeeded()

        // Verify recovery populated folders and items while preserving existing pages
        assertEquals(2, database.workspaceDao().getPageCount())
        assertTrue(database.workspaceDao().getItemCount() > 0)
        assertTrue(database.folderDao().getFolderCount() > 0)

        val folders = workspaceRepository.observeFolders().first()
        assertEquals(2, folders.size)
    }

    @Test
    fun `grid placement integrity rejects collisions on the same page`() = runBlocking {
        workspaceRepository.initializeWorkspaceIfNeeded()

        val allItemsBefore = database.workspaceDao().getAllItems()
        val initialCount = allItemsBefore.size

        // Cell (0, 0) on Page 0 is occupied by folder_creativity
        val collidingItem = WorkspaceItem(
            id = "item_collision_test",
            coordinate = GridCoordinate(
                pageIndex = 0,
                cellX = 0,
                cellY = 0,
                spanX = 1,
                spanY = 1,
                isDock = false
            ),
            itemType = WorkspaceItemType.APPLICATION,
            app = LauncherApp(id = "app_test", name = "Test", packageName = "com.albkestrel.test", isInstalledApp = true)
        )

        workspaceRepository.addItem(collidingItem)

        // Count should not increase; colliding item was rejected
        val allItemsAfter = database.workspaceDao().getAllItems()
        assertEquals(initialCount, allItemsAfter.size)
        assertNull(allItemsAfter.find { it.id == "item_collision_test" })
    }

    @Test
    fun `dock and workspace pages have independent coordinate spaces`() = runBlocking {
        workspaceRepository.initializeWorkspaceIfNeeded()

        // Dock has an item at cellX = 0, cellY = 0, isDock = true
        // Page 0 also has an item at cellX = 0, cellY = 0, isDock = false
        val allItems = database.workspaceDao().getAllItems()

        val dockItemAtOrigin = allItems.find { it.isDock && it.cellX == 0 && it.cellY == 0 }
        val pageItemAtOrigin = allItems.find { !it.isDock && it.pageId == "page_0" && it.cellX == 0 && it.cellY == 0 }

        assertNotNull(dockItemAtOrigin)
        assertNotNull(pageItemAtOrigin)
        assertEquals("dock", dockItemAtOrigin?.pageId)
        assertEquals("page_0", pageItemAtOrigin?.pageId)
    }

    @Test
    fun `folder item deduplication prevents adding duplicate packages`() = runBlocking {
        workspaceRepository.initializeWorkspaceIfNeeded()

        // Add a new unique app to folder
        val firstAdd = workspaceRepository.addAppToFolder("folder_creativity", "com.test.extra")
        assertTrue("First addition of new app must succeed", firstAdd)

        // Attempt to add the exact same package again to the same folder
        val secondAdd = workspaceRepository.addAppToFolder("folder_creativity", "com.test.extra")
        assertFalse("Duplicate package addition to the same folder must be rejected", secondAdd)

        val folderItems = database.folderDao().getFolderItems("folder_creativity")
        val extraItems = folderItems.filter { it.packageName == "com.test.extra" }
        assertEquals(1, extraItems.size)
    }

    @Test
    fun `explicit folder cascade deletion cleans up folder, items, and workspace references`() = runBlocking {
        workspaceRepository.initializeWorkspaceIfNeeded()

        // Verify folder exists before deletion
        assertNotNull(database.folderDao().getAllFolders().find { it.id == "folder_creativity" })
        assertTrue(database.folderDao().getFolderItems("folder_creativity").isNotEmpty())
        assertTrue(database.workspaceDao().getAllItems().any { it.folderId == "folder_creativity" })

        // Explicit deletion
        workspaceRepository.deleteFolder("folder_creativity")

        // Verify folder, folder items, and referencing workspace items are removed
        assertNull(database.folderDao().getAllFolders().find { it.id == "folder_creativity" })
        assertTrue(database.folderDao().getFolderItems("folder_creativity").isEmpty())
        assertFalse(database.workspaceDao().getAllItems().any { it.folderId == "folder_creativity" })
    }

    @Test
    fun `legacy prototype entries are replaced with installed applications`() = runBlocking {
        database.workspaceDao().insertPages(
            listOf(
                WorkspacePageEntity("page_0", 0),
                WorkspacePageEntity("page_1", 1)
            )
        )
        database.folderDao().insertFolders(
            listOf(
                FolderEntity("folder_creativity", "Creativity", 0xFF8B5CF6),
                FolderEntity("folder_essentials", "Essentials", 0xFF00E5FF)
            )
        )
        database.folderDao().insertFolderItems(
            listOf(
                FolderItemEntity(
                    id = "fi_old_camera",
                    folderId = "folder_creativity",
                    packageName = "com.albkestrel.camera",
                    position = 0
                )
            )
        )
        database.workspaceDao().insertItems(
            listOf(
                WorkspaceItemEntity(
                    id = "old_dock_phone",
                    itemType = "APP",
                    pageId = "dock",
                    cellX = 0,
                    cellY = 0,
                    position = 0,
                    isDock = true,
                    packageName = "com.albkestrel.dialer"
                ),
                WorkspaceItemEntity(
                    id = "old_home_weather",
                    itemType = "APP",
                    pageId = "page_0",
                    cellX = 2,
                    cellY = 0,
                    position = 2,
                    packageName = "com.albkestrel.weather"
                )
            )
        )

        workspaceRepository.initializeWorkspaceIfNeeded()

        val allItems = database.workspaceDao().getAllItems()
        assertFalse(
            "No legacy prototype package may remain in the workspace",
            allItems.any { it.packageName?.startsWith("com.albkestrel.") == true }
        )

        val dockItems = allItems.filter { it.isDock }
        assertEquals(4, dockItems.size)
        assertTrue(dockItems.all { it.packageName?.startsWith("com.test.") == true })

        val homeApps = allItems.filter { !it.isDock && it.itemType == "APP" }
        assertTrue(homeApps.isNotEmpty())
        assertTrue(homeApps.all { it.packageName?.startsWith("com.test.") == true })

        val folderItems = database.folderDao().getAllFolderItems()
        assertFalse(folderItems.any { it.packageName.startsWith("com.albkestrel.") })
        assertTrue(folderItems.isNotEmpty())
    }

    @Test
    fun `uninstalled workspace app resolves as not installed`() = runBlocking {
        workspaceRepository.initializeWorkspaceIfNeeded()

        database.workspaceDao().insertItem(
            WorkspaceItemEntity(
                id = "missing_item",
                itemType = "APP",
                pageId = "page_0",
                cellX = 0,
                cellY = 2,
                position = 20,
                packageName = "com.missing.app"
            )
        )

        val item = workspaceRepository.observeWorkspaceItems()
            .first()
            .find { it.id == "missing_item" }

        assertNotNull(item)
        assertFalse(item!!.isInstalled)
        assertNotNull(item.app)
        assertFalse(item.app!!.isInstalledApp)
        assertEquals("app", item.app!!.name)
    }

    @Test
    fun `migration 1 to 3 executes cleanly and preserves existing folder items`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "test_migration.db"
        context.deleteDatabase(dbName)

        // 1. Create a version 1 database directly
        val helper = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS `workspace_pages` (`id` TEXT NOT NULL, `pageIndex` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `workspace_folders` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `accentColorArgb` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `folder_items` (`id` TEXT NOT NULL, `folderId` TEXT NOT NULL, `packageName` TEXT NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_folder_items_folderId` ON `folder_items` (`folderId`)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `workspace_items` (`id` TEXT NOT NULL, `itemType` TEXT NOT NULL, `pageId` TEXT NOT NULL, `cellX` INTEGER NOT NULL, `cellY` INTEGER NOT NULL, `spanX` INTEGER NOT NULL, `spanY` INTEGER NOT NULL, `position` INTEGER NOT NULL, `isDock` INTEGER NOT NULL, `packageName` TEXT, `folderId` TEXT, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workspace_items_pageId` ON `workspace_items` (`pageId`)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workspace_items_folderId` ON `workspace_items` (`folderId`)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workspace_items_pageId_cellX_cellY` ON `workspace_items` (`pageId`, `cellX`, `cellY`)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )

        val v1Db = helper.writableDatabase
        v1Db.execSQL("INSERT INTO `workspace_folders` VALUES ('f_test', 'Old Folder', 123456)")
        v1Db.execSQL("INSERT INTO `folder_items` VALUES ('fi_1', 'f_test', 'com.test.pkg', 0)")
        v1Db.close()

        // 2. Open with Room specifying MIGRATION_1_2 and MIGRATION_2_3
        val migratedDb = Room.databaseBuilder(context, LauncherDatabase::class.java, dbName)
            .addMigrations(LauncherDatabase.MIGRATION_1_2, LauncherDatabase.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()

        // 3. Verify data survived migration
        val folders = runBlocking { migratedDb.folderDao().getAllFolders() }
        assertEquals(1, folders.size)
        assertEquals("Old Folder", folders[0].title)

        val folderItems = runBlocking { migratedDb.folderDao().getFolderItems("f_test") }
        assertEquals(1, folderItems.size)
        assertEquals("com.test.pkg", folderItems[0].packageName)

        val preV3Marker = runBlocking { migratedDb.workspaceMetadataDao().getValue(WorkspaceMetadataEntity.KEY_DATABASE_PRE_V3) }
        assertEquals("1", preV3Marker)

        // 4. Verify cascade deletion via ForeignKey
        migratedDb.openHelper.writableDatabase.execSQL("DELETE FROM workspace_folders WHERE id = 'f_test'")
        val itemsAfterDelete = runBlocking { migratedDb.folderDao().getFolderItems("f_test") }
        assertTrue("Cascade deletion on foreign key should remove orphaned folder items", itemsAfterDelete.isEmpty())

        migratedDb.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun `removeAppFromWorkspace removes top-level app from workspace`() = runBlocking {
        workspaceRepository.initializeWorkspaceIfNeeded()

        database.workspaceDao().insertItem(
            WorkspaceItemEntity(
                id = "item_to_remove",
                itemType = "APP",
                pageId = "page_0",
                cellX = 0,
                cellY = 1,
                position = 10,
                packageName = "com.test.removeme"
            )
        )

        val removed = workspaceRepository.removeAppFromWorkspace("com.test.removeme")
        assertTrue(removed)
        assertTrue(database.workspaceDao().getAllItems().none { it.packageName == "com.test.removeme" })
    }

    @Test
    fun `removeAppFromWorkspace removes app from folder and cleans up empty folder`() = runBlocking {
        workspaceRepository.initializeWorkspaceIfNeeded()

        val folderId = "f_temp"
        database.folderDao().insertFolder(FolderEntity(folderId, "Temp Folder", 0xFF123456))
        database.workspaceDao().insertItem(
            WorkspaceItemEntity(
                id = "ref_temp",
                itemType = "FOLDER_REF",
                pageId = "page_0",
                cellX = 0,
                cellY = 3,
                position = 30,
                folderId = folderId
            )
        )
        database.folderDao().insertFolderItem(
            FolderItemEntity("fi_temp_1", folderId, "com.test.folderapp", 0)
        )

        val removed = workspaceRepository.removeAppFromWorkspace("com.test.folderapp")
        assertTrue(removed)
        assertTrue(database.folderDao().getAllFolderItems().none { it.packageName == "com.test.folderapp" })
        assertEquals(0, database.folderDao().getFolderItems(folderId).size)
        assertTrue("Folder reference should be deleted when folder becomes empty",
            database.workspaceDao().getAllItems().none { it.folderId == folderId }
        )
    }

    @Test
    fun `removeAppFromWorkspace returns false when app is not in workspace`() = runBlocking {
        workspaceRepository.initializeWorkspaceIfNeeded()

        val removed = workspaceRepository.removeAppFromWorkspace("com.nonexistent.app")
        assertFalse(removed)
    }

    private fun testApp(name: String, packageName: String): LauncherApp {
        return LauncherApp(
            id = "test_$packageName",
            name = name,
            packageName = packageName,
            activityName = "$packageName.MainActivity",
            isInstalledApp = true
        )
    }

    private class FakeAppRepository(
        private val apps: List<LauncherApp>
    ) : AppRepository {
        override val packageChanges: Flow<Unit> = emptyFlow()

        override fun loadAllApps(): List<LauncherApp> = apps

        override fun launchApp(app: LauncherApp): Boolean = app.isInstalledApp

        override fun findAppByPackage(packageName: String): LauncherApp? {
            return apps.find { it.packageName == packageName }
        }

        override fun openAppDetails(packageName: String): Boolean = true

        override fun uninstallApp(packageName: String): Boolean = true

        override fun getShortcutsForPackage(packageName: String): List<AppShortcut> = emptyList()

        override fun launchShortcut(app: LauncherApp, shortcutId: String): Boolean = true
    }
}
