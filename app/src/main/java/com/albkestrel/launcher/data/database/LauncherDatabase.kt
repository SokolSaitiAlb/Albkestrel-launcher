package com.albkestrel.launcher.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.albkestrel.launcher.data.database.dao.FolderDao
import com.albkestrel.launcher.data.database.dao.WorkspaceDao
import com.albkestrel.launcher.data.database.entity.FolderEntity
import com.albkestrel.launcher.data.database.entity.FolderItemEntity
import com.albkestrel.launcher.data.database.entity.WorkspaceItemEntity
import com.albkestrel.launcher.data.database.entity.WorkspacePageEntity

@Database(
    entities = [
        WorkspacePageEntity::class,
        WorkspaceItemEntity::class,
        FolderEntity::class,
        FolderItemEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class LauncherDatabase : RoomDatabase() {

    abstract fun workspaceDao(): WorkspaceDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var INSTANCE: LauncherDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recreate folder_items with Foreign Key cascade constraint to workspace_folders
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `folder_items_new` (
                        `id` TEXT NOT NULL,
                        `folderId` TEXT NOT NULL,
                        `packageName` TEXT NOT NULL,
                        `position` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`folderId`) REFERENCES `workspace_folders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `folder_items_new` (`id`, `folderId`, `packageName`, `position`)
                    SELECT `id`, `folderId`, `packageName`, `position` FROM `folder_items`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `folder_items`")
                db.execSQL("ALTER TABLE `folder_items_new` RENAME TO `folder_items`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_folder_items_folderId` ON `folder_items` (`folderId`)")
            }
        }

        fun getInstance(context: Context): LauncherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LauncherDatabase::class.java,
                    "albkestrel_launcher.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
