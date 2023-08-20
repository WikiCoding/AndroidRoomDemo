package com.wikicoding.imagesstorageroom

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.wikicoding.imagesstorageroom.dao.UsersApp
import com.wikicoding.imagesstorageroom.dao.UsersDao
import com.wikicoding.imagesstorageroom.databinding.ActivityMainBinding
import com.wikicoding.imagesstorageroom.entities.TaskEntity
import com.wikicoding.imagesstorageroom.entities.UserEntity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var dao: UsersDao
    private var binding: ActivityMainBinding? = null
    private var listUsers: ArrayList<UserEntity>? = null
    private var ivImage: ImageView? = null
    /** START: Handling camera or gallery ActivityForResult **/
    private val galleryLauncherActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Uri? = result.data!!.data
            Log.e("Image", data.toString())

            ivImage?.setImageURI(data)

            val imageToSave = ivImage!!.drawable.toBitmap()

            val imagePath = saveImageToStorage(imageToSave)

            Log.e("imgPath", imagePath)

            /** Creating an user in the Db and then fetching the user by id and loading it's image to the screen **/
            val user4 = UserEntity(0, "User4", imagePath, Date())
            insertUser(user4)
            getUserById(4)
        }
    }
    private val cameraLauncherActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.e("camera", result.data!!.data.toString())
            val data: Intent? = result.data
            val thumbNail: Bitmap = data!!.extras!!.get("data") as Bitmap

            ivImage?.setImageBitmap(thumbNail)

            val imageToSave = ivImage!!.drawable.toBitmap()

            val imagePath = saveImageToStorage(imageToSave)

            Log.e("imgPath", imagePath)

            /** Creating an user in the Db and then fetching the user by id and loading it's image to the screen **/
            val user4 = UserEntity(0, "User4", imagePath, Date())
            insertUser(user4)
            getUserById(4)
        }
    }
    /** END: Handling camera or gallery ActivityForResult **/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding!!.root)

        dao = (application as UsersApp).db.usersDao()

        ivImage = binding!!.ivImage

        binding!!.btnPicture.setOnClickListener {
            /** START: Presenting the Chooser Dialog to the user and handling the selection **/
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItems = arrayOf(
                "Select photo from Gallery",
                "Capture photo from camera"
            )
            pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                        galleryLauncherActivity.launch(intent)
                    }
                    1 -> takePhotoFromCamera()
                }
            }
            pictureDialog.show()
            /** END: Presenting the Chooser Dialog to the user and handling the selection **/
        }

        /** START: Creating sample users **/
        val user1 = UserEntity(0, "User1", "", Date())
        val user2 = UserEntity(0, "User2", "", Date())
        val user3 = UserEntity(0, "User3", "", Date())

        insertUser(user1)
        insertUser(user2)
        insertUser(user3)
        /** END: Creating sample users **/

        /** START: Creating sample tasks for each user **/
        val task1 = TaskEntity(0, "Sell Stuff", 2)
        val task2 = TaskEntity(0, "Go out", 2)
        val task3 = TaskEntity(0, "Party!", 1)
        val task4 = TaskEntity(0, "Study!", 3)
        val task5 = TaskEntity(0, "Work for fun", 3)
        val task6 = TaskEntity(0, "Play Videogames", 3)

        createTask(task1)
        createTask(task2)
        createTask(task3)
        createTask(task4)
        createTask(task5)
        createTask(task6)
        /** END: Creating sample tasks for each user **/

        getAllUsers()
        getUserById(1)
        getTaskByUser(2)

        /** Go to the next Activity**/
        binding!!.btn.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }

    /** START: database operations **/
    private fun insertUser(user: UserEntity) {
        lifecycleScope.launch {
            dao.insert(user)
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
                loadImage(it.image)
            }
        }
    }

    private fun getAllUsers() {
        lifecycleScope.launch {
            dao.fetchAllUsers().collect {
                listUsers = ArrayList(it)
            }
        }
    }

    private fun createTask(taskEntity: TaskEntity) {
        lifecycleScope.launch {
            dao.insertTask(taskEntity)
        }
    }

    private fun getTaskByUser(id: Int) {
        lifecycleScope.launch {
            val data = dao.getUserTasks(id)
            /** Accessing the TaskEntity Objects **/
            data[0].tasks.forEach { taskEntity ->  Log.e("Task Entity", taskEntity.description)}
            Log.e("data", data.toString())
        }
    }
    /** END: database operations **/

    /** START: saving and loading the images **/
    private fun saveImageToStorage(bitmap: Bitmap): String {
        try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            //saving to the Downloads folder
//            val file = File("/storage/emulated/0/Download" + File.separator +
//                    "JPEG" + System.currentTimeMillis() / 1000 + ".jpg")
            /**Saving at a app data in internal storage**/
            val exportDir = this.getExternalFilesDir(null)
            if (!exportDir!!.exists()) {
                exportDir.mkdirs()
            }

            val file = File(exportDir, "${System.currentTimeMillis() / 1000}.jpg")

            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(bytes.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun loadImage(imagePath: String) {
        /**Inside the Android/data we need to request permissions**/
        requestStoragePermission {
            val filePath = File(imagePath)
            val uri = Uri.fromFile(filePath)
            Log.e("uri loaded", uri.toString())
            Glide.with(this).load(uri).into(binding!!.ivImage)
        }
    }
    /** END: saving and loading the images **/

    /** START: Handling permissions with Dexter **/
    private fun requestStoragePermission(onPermissionGranted: () -> Unit) {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        onPermissionGranted.invoke()
                    } else {
                        Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
                        showRationalDialogForPermissions()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("Permissions denied for this app")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
    /** END: Handling permissions with Dexter **/

    /** START: Options for the picture selection **/
    private fun takePhotoFromCamera() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraLauncherActivity.launch(galleryIntent)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken
            ) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }
    /** END: Options for the picture selection **/

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}