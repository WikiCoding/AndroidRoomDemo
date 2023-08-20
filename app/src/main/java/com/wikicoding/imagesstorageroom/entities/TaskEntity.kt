package com.wikicoding.imagesstorageroom.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks-table")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val taskId: Int,
    val description: String,
    val taskOwnerName: Int
)
