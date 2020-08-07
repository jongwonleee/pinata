package capstone.aiimageeditor.symmenticsegmentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import androidx.core.graphics.ColorUtils
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.collections.HashSet

class ImageSegmentationModelExecutor(
    context: Context,
    private var useGPU: Boolean = false
) {
    private var gpuDelegate: GpuDelegate? = null

    private val segmentationMasks: ByteBuffer
    private val interpreter: Interpreter

    private var numberThreads = 4

    init {
        interpreter = getInterpreter(
            context,
            imageSegmentationModel, useGPU
        )
        segmentationMasks = ByteBuffer.allocateDirect(1 * imageSize * imageSize * NUM_CLASSES * 4)
        segmentationMasks.order(ByteOrder.nativeOrder())
    }

    fun execute(data: Bitmap): ModelExecutionResult {
        try {
            var resultBitmap: Bitmap
            val value: Int
            val sourceRect = Rect(0, 0, data.width, data.height)
            val destRect: Rect

            when {
                data.width > data.height -> {
                    resultBitmap = Bitmap.createBitmap(data.width, data.width, Bitmap.Config.ARGB_8888)
                    value = data.width
                    destRect = Rect(0, (resultBitmap.height - data.height) / 2, value, (resultBitmap.height + data.height) / 2)
                }
                data.width < data.height -> {
                    resultBitmap = Bitmap.createBitmap(data.height, data.height, Bitmap.Config.ARGB_8888)
                    value = data.height
                    destRect = Rect((resultBitmap.width - data.width) / 2, 0, (resultBitmap.width + data.width) / 2, value)
                }
                else -> {
                    resultBitmap = data
                    value = data.width
                    destRect = Rect(0, 0, value, value)
                }
            }

            var mutableResultBitmap = resultBitmap.copy(Bitmap.Config.ARGB_8888, true)
            var canvas = Canvas(mutableResultBitmap)
            canvas.drawBitmap(data, sourceRect, destRect, null)

            val scaledBitmap = Bitmap.createScaledBitmap(mutableResultBitmap, imageSize, imageSize, true)
//            val scaledBitmap = ImageUtils.scaleBitmapAndKeepRatio(mutableResultBitmap, imageSize, imageSize)

            val contentArray = ImageUtils.bitmapToByteBuffer(scaledBitmap, imageSize, imageSize, IMAGE_MEAN, IMAGE_STD)

            interpreter.run(contentArray, segmentationMasks)

            val (maskImageApplied, maskOnly, itemsFound) =
                convertBytebufferMaskToBitmap(segmentationMasks, imageSize, imageSize, scaledBitmap, segmentColors)
            val scaledMaskOnly = Bitmap.createScaledBitmap(maskOnly, value, value, true)
//            val scaledMaskOnly = ImageUtils.scaleBitmapAndKeepRatio(maskOnly, value, value)

            val originSizeMaskOnly = Bitmap.createScaledBitmap(scaledMaskOnly, data.width, data.height, true)

            var mutableOriginSizeMaskOnly = originSizeMaskOnly.copy(Bitmap.Config.ARGB_8888, true)

            return ModelExecutionResult(maskImageApplied, data, mutableOriginSizeMaskOnly, itemsFound)
        } catch (e: Exception) {
            val exceptionLog = "something went wrong: ${e.message}"
            Log.d(TAG, exceptionLog)

            val emptyBitmap = ImageUtils.createEmptyBitmap(imageSize, imageSize)
            return ModelExecutionResult(emptyBitmap, emptyBitmap, emptyBitmap, HashSet(0))
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(context: Context, modelFile: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFile)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        fileDescriptor.close()
        return retFile
    }

    @Throws(IOException::class)
    private fun getInterpreter(context: Context, modelName: String, useGpu: Boolean = false): Interpreter {
        val tfliteOptions = Interpreter.Options()
        tfliteOptions.setNumThreads(numberThreads)

        gpuDelegate = null
        if (useGpu) {
            gpuDelegate = GpuDelegate()
            tfliteOptions.addDelegate(gpuDelegate)
        }

        return Interpreter(loadModelFile(context, modelName), tfliteOptions)
    }

    private val visited = Array(imageSize) { BooleanArray(imageSize) }
    private val mSegmentBits = Array(imageSize) { IntArray(imageSize) }
    private val areaArray = Array(imageSize) { IntArray(imageSize) }
    private var areaCnt: Int = 0
    private var maxArea: Int = 0
    private var bfsQueue: Queue<Pair<Int, Int>> = LinkedList<Pair<Int, Int>>()
    private var startQueue: Queue<Pair<Int, Int>> = LinkedList<Pair<Int, Int>>()

    private fun bfs(x: Int, y: Int) {
        visited[x][y] = true
        bfsQueue.add(Pair(x, y))
        while (bfsQueue.isNotEmpty()) {
            areaCnt += 1
            val temp = bfsQueue.remove()
            val tempX = temp.first
            val tempY = temp.second
            if (tempX + 1 < imageSize) {
                if (!visited[tempX + 1][tempY] && mSegmentBits[tempX + 1][tempY] == 15) {
                    visited[tempX + 1][tempY] = true
                    bfsQueue.add(Pair(tempX + 1, tempY))
                }
            }
            if (tempX - 1 >= 0) {
                if (!visited[tempX - 1][tempY] && mSegmentBits[tempX - 1][tempY] == 15) {
                    visited[tempX - 1][tempY] = true
                    bfsQueue.add(Pair(tempX - 1, tempY))
                }
            }
            if (tempY + 1 < imageSize) {
                if (!visited[tempX][tempY + 1] && mSegmentBits[tempX][tempY + 1] == 15) {
                    visited[tempX][tempY + 1] = true
                    bfsQueue.add(Pair(tempX, tempY + 1))
                }
            }
            if (tempY - 1 >= 0) {
                if (!visited[tempX][tempY - 1] && mSegmentBits[tempX][tempY - 1] == 15) {
                    visited[tempX][tempY - 1] = true
                    bfsQueue.add(Pair(tempX, tempY - 1))
                }
            }
        }
    }

    private fun bfs2(x: Int, y: Int) {
        mSegmentBits[x][y] = 0
        bfsQueue.add(Pair(x, y))
        while (bfsQueue.isNotEmpty()) {
            val temp = bfsQueue.remove()
            val tempX = temp.first
            val tempY = temp.second
            if (tempX + 1 < imageSize) {
                if (mSegmentBits[tempX + 1][tempY] == 15) {
                    mSegmentBits[tempX + 1][tempY] = 0
                    bfsQueue.add(Pair(tempX + 1, tempY))
                }
            }
            if (tempX - 1 >= 0) {
                if (mSegmentBits[tempX - 1][tempY] == 15) {
                    mSegmentBits[tempX - 1][tempY] = 0
                    bfsQueue.add(Pair(tempX - 1, tempY))
                }
            }
            if (tempY + 1 < imageSize) {
                if (mSegmentBits[tempX][tempY + 1] == 15) {
                    mSegmentBits[tempX][tempY + 1] = 0
                    bfsQueue.add(Pair(tempX, tempY + 1))
                }
            }
            if (tempY - 1 >= 0) {
                if (mSegmentBits[tempX][tempY - 1] == 15) {
                    mSegmentBits[tempX][tempY - 1] = 0
                    bfsQueue.add(Pair(tempX, tempY - 1))
                }
            }
        }
    }

    private fun convertBytebufferMaskToBitmap(
        inputBuffer: ByteBuffer,
        imageWidth: Int,
        imageHeight: Int,
        backgroundImage: Bitmap,
        colors: IntArray
    ): Triple<Bitmap, Bitmap, Set<Int>> {
        val conf = Bitmap.Config.ARGB_8888
        val maskBitmap = Bitmap.createBitmap(imageWidth, imageHeight, conf)
        val resultBitmap = Bitmap.createBitmap(imageWidth, imageHeight, conf)
        val scaledBackgroundImage = Bitmap.createScaledBitmap(backgroundImage, imageWidth, imageHeight, true)
//        val scaledBackgroundImage = ImageUtils.scaleBitmapAndKeepRatio(backgroundImage, imageWidth, imageHeight)
        val itemsFound = HashSet<Int>()
        inputBuffer.rewind()

        for (y in 0 until imageHeight) {
            for (x in 0 until imageWidth) {
                var maxVal = 0f
                mSegmentBits[x][y] = 0

                for (c in 0 until NUM_CLASSES) {
                    val value =
                        inputBuffer.getFloat((y * imageWidth * NUM_CLASSES + x * NUM_CLASSES + c) * 4)
                    if (c == 0 || value > maxVal) {
                        maxVal = value
                        mSegmentBits[x][y] = c
                    }
                }
//                itemsFound.add(mSegmentBits[x][y])
            }
        }

        for (y in 0 until imageHeight) {
            for (x in 0 until imageWidth) {
                if (!visited[x][y] && mSegmentBits[x][y] == 15) {
                    areaCnt = 0
                    bfs(x, y)
                    areaArray[x][y] = areaCnt
                    startQueue.add(Pair(x, y))
                    maxArea = maxOf(areaCnt, maxArea)
                }
            }
        }

        while (startQueue.isNotEmpty()) {
            val temp = startQueue.remove()
            if (areaArray[temp.first][temp.second] < maxArea) {
                bfs2(temp.first, temp.second)
            }
        }

        for (y in 0 until imageHeight) {
            for (x in 0 until imageWidth) {
                val newPixelColor = ColorUtils.compositeColors(
                    colors[mSegmentBits[x][y]],
                    scaledBackgroundImage.getPixel(x, y)
                )
                resultBitmap.setPixel(x, y, newPixelColor)
                maskBitmap.setPixel(x, y, colors[mSegmentBits[x][y]])
            }
        }
        return Triple(resultBitmap, maskBitmap, itemsFound)
    }

    companion object {

        private const val TAG = "ImageSegmentationMExec"
        private const val imageSegmentationModel = "deeplabv3_257_mv_gpu.tflite"
        private const val imageSize = 257
        const val NUM_CLASSES = 21
        private const val IMAGE_MEAN = 128.0f
        private const val IMAGE_STD = 128.0f


        val segmentColors = IntArray(NUM_CLASSES)
//        val labelsArrays = arrayOf(
//            "background", "aeroplane", "bicycle", "bird", "boat", "bottle", "bus",
//            "car", "cat", "chair", "cow", "dining table", "dog", "horse", "motorbike",
//            "person", "potted plant", "sheep", "sofa", "train", "tv"
//        )

        init {
            for (i in 0 until NUM_CLASSES) {
                if (i == 15)
                    segmentColors[i] = Color.WHITE
                else
                    segmentColors[i] = Color.TRANSPARENT
            }
        }
    }
}
