package capstone.aiimageeditor.ui

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.databinding.ActivitySaveBinding
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_save.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class SaveActivity : AppCompatActivity() {
    lateinit var image: Bitmap
    var imageUri: Uri? = null

    companion object {
        val SHARED_ACTIVITY = 1
    }

    private fun setImageBitmap(iv: ImageView, bitmap: Bitmap) {
        Glide.with(this).load(bitmap).into(iv)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySaveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        image = (application as ImageManager).mergeImage()
        setImageBitmap(binding.imageDone, image)

    }

    fun onBackButtonClick(v: View) {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SHARED_ACTIVITY) {
            if (imageUri != null) {
                val file = File(imageUri!!.path)
                if (file.exists()) {
                    if (file.delete()) {
                        Log.i("Pinata File Share", "File sharing done")
                    }
                }
            } else {
                Log.i("[imageUri] : ", "null")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String =
            MediaStore.Images.Media.insertImage(
                inContext.getContentResolver(),
                inImage,
                "Title",
                null
            )
        return Uri.parse(path)
    }

    fun onShareButtonClick(v: View) {
        imageUri = getImageUri(this, image)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/bmp"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivityForResult(
            shareIntent,
            SHARED_ACTIVITY
        )

    }

    fun saveImage(bitmap: Bitmap, name: String) {
        val saved: Boolean
        val fos: OutputStream
        var IMAGES_FOLDER_NAME: String = "pinata"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val mContext = this

            val resolver: ContentResolver = mContext.getContentResolver()
            val contentValues: ContentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + IMAGES_FOLDER_NAME)
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }!!
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ).toString() + File.separator + IMAGES_FOLDER_NAME

            val file = File(imagesDir)

            if (!file.exists()) {
                file.mkdir()
            }

            val image = File(imagesDir, name + ".png")
            fos = FileOutputStream(image)

        }

        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        Toast.makeText(this, "Image Saved.", Toast.LENGTH_SHORT).show()
        finish()
    }

    fun onSaveButtonClick(v: View) {
        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(Date(System.currentTimeMillis()))
        image = Bitmap.createScaledBitmap(
            image, (application as ImageManager).saveOrigianl.width, (application as ImageManager).saveOrigianl.height, true
        )
        saveImage(image, fileName)
    }
}

