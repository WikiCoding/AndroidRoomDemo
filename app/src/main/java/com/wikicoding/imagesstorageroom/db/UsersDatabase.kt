package com.wikicoding.imagesstorageroom.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wikicoding.imagesstorageroom.dao.UsersDao
import com.wikicoding.imagesstorageroom.entities.DateTimeConverter
import com.wikicoding.imagesstorageroom.entities.TaskEntity
import com.wikicoding.imagesstorageroom.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class], version = 1
)
@TypeConverters(DateTimeConverter::class)
abstract class UsersDatabase : RoomDatabase() {
    abstract fun usersDao(): UsersDao

    companion object {
        @Volatile
        private var INSTANCE: UsersDatabase? = null

        fun getInstance(context: Context): UsersDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, UsersDatabase::class.java,
                        "users_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}