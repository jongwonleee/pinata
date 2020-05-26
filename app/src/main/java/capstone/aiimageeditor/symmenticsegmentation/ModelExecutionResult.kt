package capstone.aiimageeditor.symmenticsegmentation

import android.graphics.Bitmap

data class ModelExecutionResult(
    val bitmapResult: Bitmap,
    val bitmapOriginal: Bitmap,
    val bitmapMaskOnly: Bitmap,
    val itemsFound: Set<Int>
)