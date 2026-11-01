package com.albkestrel.launcher.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workspace_pages")
data class WorkspacePageEntity(
    @PrimaryKey val id: String,
    val pageIndex: Int
)
