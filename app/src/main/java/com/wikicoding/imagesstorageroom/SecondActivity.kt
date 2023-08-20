package com.wikicoding.imagesstorageroom

import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.wikicoding.imagesstorageroom.dao.UsersApp
import com.wikicoding.imagesstorageroom.dao.UsersDao
import com.wikicoding.imagesstorageroom.databinding.ActivitySecondBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SecondActivity : AppCompatActivity() {
    private lateinit var dao: UsersDao
    private var binding: ActivitySecondBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(LayoutInflater.from(this))
        setContentView(binding!!.root)

        dao = (application as UsersApp).db.usersDao()

        getUserById(2)

        binding!!.btn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun getUserById(id: Int) {
        lifecycleScope.launch {
            dao.fetchUserById(id).collect {
                Log.e("user", it.toString())
                binding!!.tvUser.text = it.name

                val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm")
                val date = sdf.format(it.createdAt.time)

                binding!!.tvTime.text = date
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}