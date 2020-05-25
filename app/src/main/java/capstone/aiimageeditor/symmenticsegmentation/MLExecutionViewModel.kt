package capstone.aiimageeditor.symmenticsegmentation

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MLExecutionViewModel : ViewModel() {

    private val _resultingBitmap = MutableLiveData<ModelExecutionResult>()

    val resultingBitmap: LiveData<ModelExecutionResult>
        get() = _resultingBitmap

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(viewModelJob)

    fun onApplyModel(
//        filePath: String,
        imageSegmentationModel: ImageSegmentationModelExecutor,
        inferenceThread: ExecutorCoroutineDispatcher,
        imageBitamp: Bitmap
    ) {
        viewModelScope.launch(inferenceThread) {
//            val contentImage = ImageUtils.decodeBitmap(File(filePath))

            val result = imageSegmentationModel.execute(imageBitamp)
            _resultingBitmap.postValue(result)
        }
    }

}