package com.albkestrel.launcher.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Small key/value store for one-time workspace bootstrap and migration markers.
 *
 * The markers are persisted in Room so they participate in the same transaction as the workspace
 * changes they guard. That prevents a crash from leaving the database half-migrated and guarantees
 * that destructive legacy cleanup cannot run again on a later process start.
 */
@Entity(tableName = "workspace_metadata")
data class WorkspaceMetadataEntity(
    @PrimaryKey val key: String,
    val value: String
) {
    companion object {
        /**
         * Written by the 2 -> 3 migration. Its presence means the database predates the metadata
         * table, so an empty workspace must be treated as intentional rather than a fresh install.
         */
        const val KEY_DATABASE_PRE_V3 = "database_pre_v3"

        /** Set after the one-time legacy cleanup and first-install seeding have completed. */
        const val KEY_WORKSPACE_BOOTSTRAP_COMPLETE = "workspace_bootstrap_complete_v1"
    }
}
