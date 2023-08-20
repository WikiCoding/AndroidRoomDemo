package com.wikicoding.imagesstorageroom.dao

import androidx.room.*
import com.wikicoding.imagesstorageroom.entities.TaskEntity
import com.wikicoding.imagesstorageroom.entities.UserEntity
import com.wikicoding.imagesstorageroom.entities.relations.UserWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface UsersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userEntity: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(taskEntity: TaskEntity)

    @Query("SELECT * FROM `users-table`")
    fun fetchAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM `users-table` WHERE id=:id")
    fun fetchUserById(id: Int): Flow<UserEntity>

    @Transaction
    @Query("SELECT * FROM `users-table` WHERE id = :id")
    suspend fun getUserTasks(id: Int): List<UserWithTasks>
    /**calling users-table because I'm interacting with the different tables through the UserWithTasks Entity, which is a relation **/
}