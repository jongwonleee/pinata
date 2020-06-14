package capstone.aiimageeditor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.lang.Exception

class ImageManager : Application() {
    lateinit var original:Bitmap
    lateinit var mask:Bitmap
    lateinit var personOriginal:Bitmap
    lateinit var backgroundOriginal:Bitmap
    lateinit var personFiltered:Bitmap
    lateinit var backgroundFiltered:Bitmap
    private lateinit var listener:OnFinishInpaint
    var isInpainting=false

     fun getImageFromUri(selectedPhotoUri: Uri): Bitmap? {
         try{
             return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                 MediaStore.Images.Media.getBitmap(this.contentResolver, selectedPhotoUri)
             } else {
                 val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
                 ImageDecoder.decodeBitmap(source){decoder,_,_->decoder.isMutableRequired=true}
             }
         }catch (e:Exception){
             e.printStackTrace()
             return null
         }
    }
    
    fun runMaskCorrection(){
        val source= Mat()
        val mask = Mat()
        Utils.bitmapToMat(original,source)
        Utils.bitmapToMat(this@ImageManager.mask,mask)
        startMaskCorrection(source.nativeObjAddr,mask.nativeObjAddr)
        Utils.matToBitmap(mask,this.mask)
        
    }

    fun setOnFinishInpaint(listener:OnFinishInpaint){
        this.listener=listener
    }

    fun startInpaint(){
        backgroundOriginal = Bitmap.createBitmap(original)
        InpaintTask().execute(0)
    }

    fun resetImages(){
        original = Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)
        mask = Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)
        personOriginal=Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)
        backgroundOriginal=Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)
        personFiltered
    }

    fun loadOriginal(uri:Uri): Boolean {
        var image =getImageFromUri(uri)
        if(image!=null){
            original = image
            backgroundOriginal = Bitmap.createBitmap(original)
            backgroundFiltered = Bitmap.createBitmap(original)
            return true
        }else return false


    }

    fun mergeImage():Bitmap{
        val bitmap = Bitmap.createBitmap(backgroundFiltered)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(personFiltered,0f,0f,null)
        return bitmap
    }

    interface OnFinishInpaint{
        fun onFinishInpaint()
    }

    inner class InpaintTask: AsyncTask<Int, Int, Bitmap>() {
        override fun doInBackground(vararg p0: Int?): Bitmap {
            isInpainting=true
            val source= Mat()
            val mask = Mat()
            Utils.bitmapToMat(original,source)
            Utils.bitmapToMat(this@ImageManager.mask,mask)
            startInpaint(source.nativeObjAddr,mask.nativeObjAddr)
            val bitmap = Bitmap.createBitmap(original.width,original.height,Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(source,bitmap)
            return bitmap
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            if(result!=null){
                backgroundOriginal=result
                backgroundFiltered=result.copy(Bitmap.Config.ARGB_8888,true)
                isInpainting=false
                Log.i("!!","inpaint finished")
                listener.onFinishInpaint()
            }
        }

        external fun startInpaint(image: Long, mask: Long)
    }
    external fun startMaskCorrection(sourceImage:Long, mask:Long)

}