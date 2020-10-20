package capstone.aiimageeditor.ui

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.NavHostController
import androidx.navigation.fragment.findNavController
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.databinding.FragmentSaveBinding
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class FragmentSave : BaseKotlinFragment<FragmentSaveBinding>() {
    override val layoutResourceId: Int
        get() = R.layout.fragment_save

    lateinit var image: Bitmap
    var imageUri: Uri? = null

    companion object {
        const val SHARED_ACTIVITY = 1
    }


    override fun initStartView() {
        image = (requireActivity().application as ImageManager).mergeImage()
        setImageBitmap(binding.imageDone, image)
    }

    override fun initDataBinding() {
    }

    override fun initAfterBinding() {
        binding.buttonShare.setOnClickListener {
            imageUri = getImageUri(requireContext(), image)
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
        binding.floatingActionButton.setOnClickListener {
            val fileName = SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(Date(System.currentTimeMillis()))
            image = Bitmap.createScaledBitmap(
                image, (requireActivity().application as ImageManager).saveOrigianl.width, (requireActivity().application as ImageManager).saveOrigianl.height, true
            )
            saveImage(image, fileName)
        }
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun reLoadUI() {
    }

    private fun setImageBitmap(iv: ImageView, bitmap: Bitmap) {
        Glide.with(this).load(bitmap).into(iv)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SHARED_ACTIVITY) {
            if (imageUri != null) {
                val file = File(imageUri!!.path)
                if (file.exists() && file.delete()) {
                    Log.i("Pinata File Share", "File sharing done")
                }
            } else {
                Log.i("[imageUri] : ", "null")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String =
            MediaStore.Images.Media.insertImage(
                inContext.contentResolver,
                inImage,
                "Title",
                null
            )
        return Uri.parse(path)
    }


    private fun saveImage(bitmap: Bitmap, name: String) {
        val saved: Boolean
        val fos: OutputStream
        val IMAGES_FOLDER_NAME: String = resources.getString(R.string.app_name)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val mContext = requireContext()

            val resolver: ContentResolver = mContext.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, resources.getString(R.string.save_type))
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, resources.getString(R.string.save_location) + IMAGES_FOLDER_NAME)
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

            val image = File(imagesDir, name + resources.getString(R.string.save_extension))
            fos = FileOutputStream(image)

        }

        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        Toast.makeText(requireContext(), "Image Saved.", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

}