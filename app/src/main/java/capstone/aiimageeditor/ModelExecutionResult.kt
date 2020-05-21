package capstone.aiimageeditor

import android.graphics.Bitmap

data class ModelExecutionResult(
    val bitmapResult: Bitmap,
    val bitmapOriginal: Bitmap,
    val bitmapMaskOnly: Bitmap,
    val bitmapMaskWithOrigin : Bitmap,
    val bitmapMaskWithoutOrigin : Bitmap,
    val itemsFound: Set<Int>
)