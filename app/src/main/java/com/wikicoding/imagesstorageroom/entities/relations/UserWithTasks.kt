package com.wikicoding.imagesstorageroom.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.wikicoding.imagesstorageroom.entities.TaskEntity
import com.wikicoding.imagesstorageroom.entities.UserEntity

data class UserWithTasks(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskOwnerName"
    )
    val tasks: List<TaskEntity>
)
