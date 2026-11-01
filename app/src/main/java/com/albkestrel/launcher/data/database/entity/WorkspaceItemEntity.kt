package com.albkestrel.launcher.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workspace_items",
    indices = [
        Index("pageId"),
        Index("folderId"),
        Index(value = ["pageId", "cellX", "cellY"], unique = false)
    ]
)
data class WorkspaceItemEntity(
    @PrimaryKey val id: String,
    val itemType: String,             // "APP", "FOLDER_REF", "WIDGET_SLOT"
    val pageId: String,               // Page id or "dock"
    val cellX: Int,
    val cellY: Int,
    val spanX: Int = 1,
    val spanY: Int = 1,
    val position: Int = 0,
    val isDock: Boolean = false,
    val packageName: String? = null,
    val folderId: String? = null
)
