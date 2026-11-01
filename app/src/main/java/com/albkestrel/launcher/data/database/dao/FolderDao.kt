package com.albkestrel.launcher.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.albkestrel.launcher.data.database.entity.FolderEntity
import com.albkestrel.launcher.data.database.entity.FolderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM workspace_folders ORDER BY id ASC")
    fun observeFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM workspace_folders ORDER BY id ASC")
    suspend fun getAllFolders(): List<FolderEntity>

    @Query("SELECT COUNT(*) FROM workspace_folders")
    suspend fun getFolderCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Query("UPDATE workspace_folders SET title = :title WHERE id = :folderId")
    suspend fun renameFolder(folderId: String, title: String)

    @Query("DELETE FROM workspace_folders WHERE id = :folderId")
    suspend fun deleteFolder(folderId: String)

    @Query("SELECT * FROM folder_items ORDER BY folderId ASC, position ASC")
    fun observeFolderItems(): Flow<List<FolderItemEntity>>

    @Query("SELECT * FROM folder_items WHERE folderId = :folderId ORDER BY position ASC")
    suspend fun getFolderItems(folderId: String): List<FolderItemEntity>

    @Query("SELECT COUNT(*) FROM folder_items WHERE folderId = :folderId AND packageName = :packageName")
    suspend fun countFolderItem(folderId: String, packageName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolderItem(item: FolderItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolderItems(items: List<FolderItemEntity>)

    @Query("DELETE FROM folder_items WHERE id = :id")
    suspend fun deleteFolderItem(id: String)

    @Query("DELETE FROM folder_items WHERE folderId = :folderId")
    suspend fun deleteFolderItemsByFolderId(folderId: String)

    @Transaction
    suspend fun deleteFolderCascade(folderId: String) {
        deleteFolderItemsByFolderId(folderId)
        deleteFolder(folderId)
    }
}
