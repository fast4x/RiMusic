package me.knighthat.database.migrator

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec

@DeleteTable.Entries(
    DeleteTable( tableName = "QueuedMediaItem" )
)
class From3To4Migration : AutoMigrationSpec