package capstone.aiimageeditor.symmenticsegmentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class ImageUtils {
    companion object {
        private fun decodeExifOrientation(orientation: Int): Matrix {
            val matrix = Matrix()

            when (orientation) {
                ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_UNDEFINED -> Unit
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1F, 1F)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1F, -1F)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postScale(-1F, 1F)
                    matrix.postRotate(270F)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postScale(-1F, 1F)
                    matrix.postRotate(90F)
                }

                else -> throw IllegalArgumentException("Invalid orientation: $orientation")
            }

            return matrix
        }

        fun setExifOrientation(
            filePath: String,
            value: String
        ) {
            val exif = ExifInterface(filePath)
            exif.setAttribute(
                ExifInterface.TAG_ORIENTATION, value
            )
            exif.saveAttributes()
        }

        fun computeExifOrientation(rotationDegrees: Int, mirrored: Boolean) = when {
            rotationDegrees == 0 && !mirrored -> ExifInterface.ORIENTATION_NORMAL
            rotationDegrees == 0 && mirrored -> ExifInterface.ORIENTATION_FLIP_HORIZONTAL
            rotationDegrees == 180 && !mirrored -> ExifInterface.ORIENTATION_ROTATE_180
            rotationDegrees == 180 && mirrored -> ExifInterface.ORIENTATION_FLIP_VERTICAL
            rotationDegrees == 270 && mirrored -> ExifInterface.ORIENTATION_TRANSVERSE
            rotationDegrees == 90 && !mirrored -> ExifInterface.ORIENTATION_ROTATE_90
            rotationDegrees == 90 && mirrored -> ExifInterface.ORIENTATION_TRANSPOSE
            rotationDegrees == 270 && mirrored -> ExifInterface.ORIENTATION_ROTATE_270
            rotationDegrees == 270 && !mirrored -> ExifInterface.ORIENTATION_TRANSVERSE
            else -> ExifInterface.ORIENTATION_UNDEFINED
        }

        fun decodeBitmap(file: File): Bitmap {
            Log.e("[decodeBitmap]", file.absolutePath)
            val exif = ExifInterface(file.absolutePath)
            val transformation =
                decodeExifOrientation(
                    exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90
                    )
                )

            val options = BitmapFactory.Options()
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            return Bitmap.createBitmap(
                BitmapFactory.decodeFile(file.absolutePath),
                0, 0, bitmap.width, bitmap.height, transformation, true
            )
        }

        fun scaleBitmapAndKeepRatio(
            targetBmp: Bitmap,
            reqHeightInPixels: Int,
            reqWidthInPixels: Int
        ): Bitmap {
            if (targetBmp.height == reqHeightInPixels && targetBmp.width == reqWidthInPixels) {
                return targetBmp
            }
            val matrix = Matrix()
            matrix.setRectToRect(
                RectF(0f, 0f, targetBmp.width.toFloat(), targetBmp.width.toFloat()),
                RectF(0f, 0f, reqWidthInPixels.toFloat(), reqHeightInPixels.toFloat()),
                Matrix.ScaleToFit.FILL
            )
            return Bitmap.createBitmap(
                targetBmp, 0, 0,
                targetBmp.width,
                targetBmp.width, matrix, true
            )
        }

        fun bitmapToByteBuffer(
            bitmapIn: Bitmap,
            width: Int,
            height: Int,
            mean: Float = 0.0f,
            std: Float = 255.0f
        ): ByteBuffer {
            val bitmap = bitmapIn

            val inputImage = ByteBuffer.allocateDirect(1 * width * height * 3 * 4)
            inputImage.order(ByteOrder.nativeOrder())
            inputImage.rewind()

            val intValues = IntArray(width * height)
            bitmap.getPixels(intValues, 0, width, 0, 0, width, height)
            var pixel = 0
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val value = intValues[pixel++]

                    inputImage.putFloat(((value shr 16 and 0xFF) - mean) / std)
                    inputImage.putFloat(((value shr 8 and 0xFF) - mean) / std)
                    inputImage.putFloat(((value and 0xFF) - mean) / std)
                }
            }

            inputImage.rewind()
            return inputImage
        }

        fun createEmptyBitmap(imageWidth: Int, imageHeigth: Int, color: Int = 0): Bitmap {
            val ret = Bitmap.createBitmap(imageWidth, imageHeigth, Bitmap.Config.ARGB_8888)
            if (color != 0) {
                ret.eraseColor(color)
            }
            return ret
        }
    }
}
