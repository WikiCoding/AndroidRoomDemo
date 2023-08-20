package com.wikicoding.imagesstorageroom.dao

import android.app.Application
import com.wikicoding.imagesstorageroom.db.UsersDatabase

class UsersApp: Application() {
    val db by lazy {
        UsersDatabase.getInstance(this)
    }
}