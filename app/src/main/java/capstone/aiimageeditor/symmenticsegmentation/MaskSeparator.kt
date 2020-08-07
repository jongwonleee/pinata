package capstone.aiimageeditor.symmenticsegmentation

import android.graphics.*

class MaskSeparator {
    fun applyWithMask(mainImage: Bitmap, maskImage: Bitmap): Bitmap {
        val canvas = Canvas()

        val result = Bitmap.createBitmap(mainImage.width, mainImage.height, Bitmap.Config.ARGB_8888)

        canvas.setBitmap(result)
        val paint = Paint()

        // resize image fills the whole canvas
        canvas.drawBitmap(mainImage, null, Rect(0, 0, mainImage.width, mainImage.height), paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawBitmap(maskImage, null, Rect(0, 0, mainImage.width, mainImage.height), paint)
        paint.xfermode = null
        return result
    }

    fun applyWithoutMask(mainImage: Bitmap, maskImage: Bitmap): Bitmap {
        val canvas = Canvas()

        val result = Bitmap.createBitmap(mainImage.width, mainImage.height, Bitmap.Config.ARGB_8888)

        canvas.setBitmap(result)
        val paint = Paint()

        // resize image fills the whole canvas
        canvas.drawBitmap(mainImage, null, Rect(0, 0, mainImage.width, mainImage.height), paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        canvas.drawBitmap(maskImage, null, Rect(0, 0, mainImage.width, mainImage.height), paint)
        paint.xfermode = null

        return result
    }
}