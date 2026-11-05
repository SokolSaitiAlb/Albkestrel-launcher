package com.albkestrel.launcher.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.albkestrel.launcher.data.database.entity.WorkspaceItemEntity
import com.albkestrel.launcher.data.database.entity.WorkspacePageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {

    @Query("SELECT * FROM workspace_pages ORDER BY pageIndex ASC")
    fun observePages(): Flow<List<WorkspacePageEntity>>

    @Query("SELECT COUNT(*) FROM workspace_pages")
    suspend fun getPageCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: WorkspacePageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<WorkspacePageEntity>)

    @Query("SELECT * FROM workspace_items ORDER BY isDock ASC, pageId ASC, cellY ASC, cellX ASC, position ASC")
    fun observeItems(): Flow<List<WorkspaceItemEntity>>

    @Query("SELECT * FROM workspace_items ORDER BY isDock ASC, pageId ASC, cellY ASC, cellX ASC, position ASC")
    suspend fun getAllItems(): List<WorkspaceItemEntity>

    @Query("SELECT COUNT(*) FROM workspace_items")
    suspend fun getItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WorkspaceItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<WorkspaceItemEntity>)

    @Update
    suspend fun updateItem(item: WorkspaceItemEntity)

    @Delete
    suspend fun deleteItem(item: WorkspaceItemEntity)

    @Query("DELETE FROM workspace_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM workspace_items WHERE packageName IN (:packageNames)")
    suspend fun deleteItemsByPackageNames(packageNames: List<String>)

    @Query("DELETE FROM workspace_items WHERE folderId = :folderId")
    suspend fun deleteItemsByFolderId(folderId: String)

    @Query("SELECT * FROM workspace_items WHERE pageId = :pageId AND cellX = :cellX AND cellY = :cellY LIMIT 1")
    suspend fun getItemAt(pageId: String, cellX: Int, cellY: Int): WorkspaceItemEntity?
}
