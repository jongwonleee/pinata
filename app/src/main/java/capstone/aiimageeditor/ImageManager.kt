package capstone.aiimageeditor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

class ImageManager : Application() {
    lateinit var original:Bitmap
    lateinit var mask:Bitmap
    lateinit var person:Bitmap
    lateinit var background:Bitmap

     fun getImageFromUri(selectedPhotoUri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            MediaStore.Images.Media.getBitmap(this.contentResolver, selectedPhotoUri)
        } else {
            val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
            ImageDecoder.decodeBitmap(source){decoder,_,_->decoder.isMutableRequired=true}
        }
    }
    fun loadMask(uri:Uri) {
        mask = getImageFromUri(uri)
    }
    fun loadOriginal(uri:Uri) {
        original = getImageFromUri(uri)
    }
}