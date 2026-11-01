package com.albkestrel.launcher.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workspace_folders")
data class FolderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val accentColorArgb: Long = 0xFF00E5FF
)
