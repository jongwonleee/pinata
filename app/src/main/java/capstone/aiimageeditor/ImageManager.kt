package capstone.aiimageeditor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import org.opencv.android.Utils
import org.opencv.core.Mat

class ImageManager : Application() {
    lateinit var original: Bitmap
    lateinit var saveOrigianl: Bitmap
    lateinit var mask: Bitmap
    lateinit var personOriginal: Bitmap
    lateinit var backgroundOriginal: Bitmap
    lateinit var personFiltered: Bitmap
    lateinit var backgroundFiltered: Bitmap


    var personFilters = arrayListOf<GPUImageFilter?>()
    var personAdjusts = arrayListOf<Int>()
    var doHalo = false
    var haloColor = Color.WHITE

    var backgroundFilters = arrayListOf<GPUImageFilter?>()
    var backgroundAdjusts = arrayListOf<Int>()

    fun initialize() {
        haloColor = Color.WHITE
        personFilters.clear()
        personAdjusts.clear()
        backgroundFilters.clear()
        backgroundAdjusts.clear()
        for (i in 0..9) {
            personFilters.add(null)
            personAdjusts.add(50)
        }
        for (i in 0..8) {
            backgroundFilters.add(null)
            backgroundAdjusts.add(50)
        }
        personAdjusts[4] = 0
        personAdjusts[8] = 0
        doHalo = false
        backgroundAdjusts[5] = 0
    }

    private lateinit var listener: OnFinishInpaint
    var isInpainting = false

    fun getImageFromUri(selectedPhotoUri: Uri): Bitmap? {
        try {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                MediaStore.Images.Media.getBitmap(this.contentResolver, selectedPhotoUri)
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ -> decoder.isMutableRequired = true }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun runMaskCorrection() {
        val source = Mat()
        val mask = Mat()
        Utils.bitmapToMat(original, source)
        Utils.bitmapToMat(this@ImageManager.mask, mask)
        startMaskCorrection(source.nativeObjAddr, mask.nativeObjAddr)
        Utils.matToBitmap(mask, this.mask)

    }

    fun setOnFinishInpaint(listener: OnFinishInpaint) {
        this.listener = listener
    }

    fun startInpaint() {

        backgroundOriginal = Bitmap.createBitmap(original)
        InpaintTask().execute(0)


/*        var input = Bitmap.createBitmap(original)
        val maskimage = Bitmap.createBitmap(mask)

        var W: Int = maskimage.getWidth()
        var H: Int = maskimage.getHeight()
        val mask = Array(W) { BooleanArray(H) }
        for (y in 0 until H)
            for (x in 0 until W)
                mask[x][y] = maskimage.getPixel(x,y) == Color.WHITE

        W = input.getWidth()
        H = input.getHeight()
        for (y in 0 until H)
            for (x in 0 until W)
                if (mask[x][y]) input.setPixel(x, y, -0x10000)

        val result = Inpaint().inpaint(input, mask, 2);
        backgroundOriginal=result
        backgroundFiltered=result.copy(Bitmap.Config.ARGB_8888,true)
        isInpainting=false
        Log.i("!!","inpaint finished")
        listener.onFinishInpaint()*/
    }


    fun loadOriginal(uri: Uri): Boolean {
        var image = getImageFromUri(uri)
        return if (image != null) {
            original = image
            saveOrigianl = image

            var tempWidth = original.width
            var tempHeight = original.height
            while (tempWidth * tempHeight >= 1000000) {
                tempWidth /= 2
                tempHeight /= 2
            }
            original = Bitmap.createScaledBitmap(original, tempWidth, tempHeight, true)
            backgroundOriginal = Bitmap.createBitmap(original)
            backgroundFiltered = Bitmap.createBitmap(original)
            true
        } else false


    }

    fun mergeImage(): Bitmap {
        val bitmap = Bitmap.createBitmap(backgroundFiltered)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(personFiltered, 0f, 0f, null)
        return bitmap
    }

    interface OnFinishInpaint {
        fun onFinishInpaint()
    }

    inner class InpaintTask : AsyncTask<Int, Int, Bitmap>() {
        override fun doInBackground(vararg p0: Int?): Bitmap {
            isInpainting = true
            val source = Mat()
            val mask = Mat()
            Utils.bitmapToMat(original, source)
            Utils.bitmapToMat(this@ImageManager.mask, mask)
            startInpaint(source.nativeObjAddr, mask.nativeObjAddr)
            val bitmap = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(source, bitmap)
            return bitmap
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            if (result != null) {
                backgroundOriginal = result
                backgroundFiltered = result.copy(Bitmap.Config.ARGB_8888, true)
                isInpainting = false
                Log.i("!!", "inpaint finished")
                listener.onFinishInpaint()
            }
        }

        private external fun startInpaint(image: Long, mask: Long)
    }

    private external fun startMaskCorrection(sourceImage: Long, mask: Long)

}