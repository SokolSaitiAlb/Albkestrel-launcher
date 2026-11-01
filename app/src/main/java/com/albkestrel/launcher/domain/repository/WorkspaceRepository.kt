package com.albkestrel.launcher.domain.repository

import com.albkestrel.launcher.domain.model.GridCoordinate
import com.albkestrel.launcher.domain.model.WorkspaceFolder
import com.albkestrel.launcher.domain.model.WorkspaceItem
import com.albkestrel.launcher.domain.model.WorkspacePage
import kotlinx.coroutines.flow.Flow

interface WorkspaceRepository {
    fun observePages(): Flow<List<WorkspacePage>>
    fun observeWorkspaceItems(): Flow<List<WorkspaceItem>>
    fun observeFolders(): Flow<List<WorkspaceFolder>>
    suspend fun initializeWorkspaceIfNeeded()
    suspend fun renameFolder(folderId: String, newTitle: String)
    suspend fun moveItem(itemId: String, newCoord: GridCoordinate)
    suspend fun addItem(item: WorkspaceItem)
    suspend fun removeItem(itemId: String)
    suspend fun deleteFolder(folderId: String)
    suspend fun addAppToFolder(folderId: String, packageName: String): Boolean
}
