package capstone.aiimageeditor

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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import kotlinx.android.synthetic.main.activity_save.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class SaveActivity : AppCompatActivity() {
    lateinit var image: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save)
        image = (application as ImageManager).original
        image_done.setImageBitmap(image)
    }

    fun onBackButtonClick(v: View) {
        finish()
    }

    fun onDeleteButtonClick(v: View) {
        //TODO stack 구현 후 undo 구현
    }
    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String =
            MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
        return Uri.parse(path)
    }
    fun onShareButtonClick(v: View){
        val imageUri = getImageUri(this,image)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/bmp"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
        if(imageUri != null) {
            val file = File(imageUri.path)
            file.delete()
            if (file.exists()) {
                if (file.delete()) {
                    Log.i("Pinata File Share", "File sharing done")
                }
            }
        }
        else{
            Log.i("[imageUri] : ", "null")
        }
    }

    //    fun saveImage():Uri{
//        val ex_storage =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();//getExternalFilesDir(null)!!.absolutePath;
//        val foler_name = "/Pinata/"
//        val file_name = SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(Date(System.currentTimeMillis()))+".jpg"
//        val string_path = ex_storage+foler_name
//        Log.i("Pinata File Saving", "Saving $file_name on $string_path")
//        lateinit var file_path:File
//        try{
//            file_path = File(string_path)
//            if(!file_path.exists()){
//                Log.i("Pinata File Saving", "$string_path not exists. Creating folder")
//                file_path.mkdirs()
//            }
//            val out = FileOutputStream(string_path+file_name)
//            image.compress(Bitmap.CompressFormat.JPEG, 100, out)
//            out.close();
//            Log.i("Pinata File Saving", "File saved completely")
//        }catch(exception: FileNotFoundException){
//            Log.e("FileNotFoundException", "${exception.message}")
//            return Uri.EMPTY
//        }catch(exception: IOException){
//            Log.e("IOException", "${exception.message}")
//            return Uri.EMPTY
//        }
//        return Uri.parse(string_path)
//    }
    private fun saveImage(bitmap: Bitmap, name: String){
        val saved: Boolean
        val fos: OutputStream
        var IMAGES_FOLDER_NAME: String = "myPhoto"

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
        val fileName =
            SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(Date(System.currentTimeMillis()))
        saveImage(image, fileName)
//        val uri = saveImage()
//        if(uri==null || Uri.EMPTY.equals(uri)){
//            Log.i("Pinata File Saving","File Not Saved")
//            Toast.makeText(this,"저장에 실패했습니다. 다시 한번 시도해주세요.",Toast.LENGTH_LONG)
//        }else
//        {
//            Log.i("Pinata File Saving","File Saved")
//            Toast.makeText(this,"저장 완료.",Toast.LENGTH_SHORT)
    }
}

//
//package capstone.aiimageeditor
//
//import android.content.Intent
//import android.graphics.Bitmap
//import android.net.Uri
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.os.Environment
//import android.provider.MediaStore
//import android.util.Log
//import android.view.View
//import android.widget.Toast
//import androidx.core.net.toFile
//import kotlinx.android.synthetic.main.activity_save.*
//import java.io.File
//import java.io.FileNotFoundException
//import java.io.FileOutputStream
//import java.io.IOException
//import java.text.SimpleDateFormat
//import java.util.*
//
//class SaveActivity : AppCompatActivity() {
//    lateinit var image:Bitmap
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_save)
//        image = (application as ImagePasser).image
//        image_done.setImageBitmap(image)
//    }
//    fun onBackButtonClick(v: View){
//        finish()
//    }
//    fun onDeleteButtonClick(v: View){
//        //TODO stack 구현 후 undo 구현
//    }
//
//    fun onShareButtonClick(v: View){
//        val uri = saveImage()
//        val sendIntent: Intent = Intent().apply {
//            action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_STREAM, uri)
//            type = "image/bmp"
//        }
//        val shareIntent = Intent.createChooser(sendIntent, null)
//        startActivity(shareIntent)
//        val file = uri.toFile()
//        if(file.exists()){
//            if(file.delete()){
//                Log.i("Pinata File Share", "File sharing done")
//            }
//        }
//    }
//
//    fun saveImage():Uri{
//        val ex_storage =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();//getExternalFilesDir(null)!!.absolutePath;
//        val foler_name = "/Pinata/"
//        val file_name = SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(Date(System.currentTimeMillis()))+".jpg"
//        val string_path = ex_storage+foler_name
//        Log.i("Pinata File Saving", "Saving $file_name on $string_path")
//        lateinit var file_path:File
//        try{
//            file_path = File(string_path)
//            if(!file_path.exists()){
//                Log.i("Pinata File Saving", "$string_path not exists. Creating folder")
//                file_path.mkdirs()
//            }
//            val out = FileOutputStream(string_path+file_name)
//            image.compress(Bitmap.CompressFormat.JPEG, 100, out)
//            out.close();
//            Log.i("Pinata File Saving", "File saved completely")
//        }catch(exception: FileNotFoundException){
//            Log.e("FileNotFoundException", "${exception.message}")
//            return Uri.EMPTY
//        }catch(exception: IOException){
//            Log.e("IOException", "${exception.message}")
//            return Uri.EMPTY
//        }
//        return Uri.parse(string_path)
//    }
//    fun onSaveButtonClick(v: View){
//        val uri = saveImage()
//        if(uri==null || Uri.EMPTY.equals(uri)){
//            Log.i("Pinata File Saving","File Not Saved")
//            Toast.makeText(this,"저장에 실패했습니다. 다시 한번 시도해주세요.",Toast.LENGTH_LONG)
//        }else
//        {
//            Log.i("Pinata File Saving","File saved")
//            Toast.makeText(this,"저장 완료.",Toast.LENGTH_SHORT)
//        }
//    }
//
//}
