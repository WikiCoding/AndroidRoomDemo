package com.wikicoding.imagesstorageroom.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.*

@Entity(tableName = "users-table")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val image: String, /** Storing it in the Internal Storage and Saving the Uri String here **/
    val createdAt: Date
)