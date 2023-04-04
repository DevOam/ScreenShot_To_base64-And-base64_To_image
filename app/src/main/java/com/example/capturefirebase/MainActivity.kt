package com.example.capturefirebase

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.*
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyStoragePermission(this)
        val btnPermissions = findViewById<Button>(R.id.btn_permissions)
        val dini = findViewById<Button>(R.id.dini)
        val imageView = findViewById<ImageView>(R.id.imageView)

        btnPermissions.setOnClickListener {
            val screenshotFile = takeScreenshot(window.decorView.rootView, "screenshotResult")
            if (screenshotFile != null) {
                Glide.with(this)
                    .load(screenshotFile)
                    .into(imageView)
                Toast.makeText(this, "Successfully captured and loaded screenshot", Toast.LENGTH_SHORT).show()
                val base64String = convertToBase64(screenshotFile)
                Log.d("Soso", "Base64 string: $base64String")
                val intent  = Intent(this, Second::class.java)
                intent.putExtra("dini", base64String)
                GlobalScope.launch {
                    delay(2000L)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "Failed to capture screenshot", Toast.LENGTH_SHORT).show()
            }
        }
//        dini.setOnClickListener {
//
//        }
    }

    private fun takeScreenshot(view: View, fileName: String): File? {
        // Saving the image based upon the date
        val pattern = "yyyy-MM-dd_hh:mm:ss"
        val simpleDateFormat = SimpleDateFormat(pattern)

        try {
            val dirPath = "${Environment.getExternalStorageDirectory()}/androidScreenshot"
            val fileDir = File(dirPath)

            if (!fileDir.exists()) {
                val mkdir = fileDir.mkdir()
            }

            val path = "$dirPath/$fileName-${simpleDateFormat.format(System.currentTimeMillis())}.png"

            view.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false

            val imageFile = File(path)

            val fileOutputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            return imageFile
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    private fun convertToBase64(file: File): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val fileInputStream = FileInputStream(file)

        val buffer = ByteArray(1024)
        var len: Int
        while (fileInputStream.read(buffer).also { len = it } != -1) {
            byteArrayOutputStream.write(buffer, 0, len)
        }
        byteArrayOutputStream.flush()

        val base64String = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

        fileInputStream.close()
        byteArrayOutputStream.close()

        return base64String
    }

    private fun verifyStoragePermission(activity: Activity) {
        val permission = ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
    }
}
