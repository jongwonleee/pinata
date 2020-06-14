package capstone.aiimageeditor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class ImageHalo {
    private lateinit var colors: IntArray

    private var weight: Int = 110

    private var coordQueue: Queue<Triple<Int, Int, Pair<Int, Int>>> = LinkedList()
    private var width: Int = 1
    private var height: Int = 1
    private var totalSize: Int = 1
    private var color: Int = 0
    var doHalo = false

    fun run(inputImage: Bitmap): Bitmap {
        width = inputImage.width
        height = inputImage.height
        totalSize = width * height
        var scaledInputBitmap: Bitmap = inputImage
        var isScaled = false

        while (totalSize > 1000000) {
            width /= 2
            height /= 2
            totalSize /= 4
            scaledInputBitmap = Bitmap.createScaledBitmap(scaledInputBitmap, width, height, true)
            isScaled = true
        }

        colors = IntArray(scaledInputBitmap.width * scaledInputBitmap.height)
        scaledInputBitmap.getPixels(colors, 0, scaledInputBitmap.width, 0, 0, scaledInputBitmap.width, scaledInputBitmap.height)

        width = scaledInputBitmap.width
        height = scaledInputBitmap.height
        val widthMax = scaledInputBitmap.width / weight
        val heightMax = scaledInputBitmap.height / weight
        val distMax = widthMax.coerceAtMost(heightMax)
        var dist: Array<Array<Float>> = Array(width) { Array(height) { distMax.toFloat() } }

        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        val alpha = Color.alpha(color)

        for (i in colors.indices) {
            if (colors[i] != Color.TRANSPARENT) {
                val x = i % width
                val y = i / width
                val xDirection = checkX(x, i)
                val yDirection = checkY(y, i)
                if (xDirection != -1 || yDirection != -1)
                    coordQueue.add(Triple(x, y, Pair(xDirection, yDirection)))
            }
        }

        while (coordQueue.isNotEmpty()) {
            val temp = coordQueue.remove()
            val x = temp.first
            val y = temp.second
            val i = x + y * width
            val xDirection = temp.third.first
            val yDirection = temp.third.second

            if (yDirection == -1) {
                for (w in 0 until widthMax) {
                    val wDistance = distance(w, 0)
                    val newColor =
                        Color.argb(alpha * (widthMax - w) / widthMax, red, green, blue)
                    when (xDirection) {
                        0 -> {
                            if (x >= w)
                                if (i >= w)
                                    if (colors[i - w] == Color.TRANSPARENT)
                                        if (dist[x - w][y] >= wDistance) {
                                            scaledInputBitmap.setPixel(x - w, y, newColor)
                                            dist[x - w][y] = wDistance;
                                        }
                        }
                        1 -> {
                            if (x < width - w)
                                if (i < totalSize - w)
                                    if (colors[i + w] == Color.TRANSPARENT)
                                        if (dist[x + w][y] >= wDistance) {
                                            scaledInputBitmap.setPixel(x + w, y, newColor)
                                            dist[x + w][y] = wDistance;
                                        }
                        }
                        2 -> {
                            if (x > w)
                                if (i >= w)
                                    if (colors[i - w] == Color.TRANSPARENT)
                                        if (dist[x - w][y] >= wDistance) {
                                            scaledInputBitmap.setPixel(x - w, y, newColor)
                                            dist[x - w][y] = wDistance;
                                        }
                            if (x < width - w)
                                if (i < totalSize - w)
                                    if (colors[i + w] == Color.TRANSPARENT)
                                        if (dist[x + w][y] >= wDistance) {
                                            scaledInputBitmap.setPixel(x + w, y, newColor)
                                            dist[x + w][y] = wDistance;
                                        }
                        }
                    }
                }
            } else if (xDirection == -1) {
                for (h in 0 until heightMax) {
                    val yDistance = distance(0, h)
                    val newColor =
                        Color.argb(alpha * (heightMax - h) / heightMax, red, green, blue)
                    when (yDirection) {
                        0 -> {
                            if (y > h)
                                if (i >= h * width)
                                    if (colors[i - h * width] == Color.TRANSPARENT)
                                        if (dist[x][y - h] >= yDistance) {
                                            scaledInputBitmap.setPixel(x, y - h, newColor)
                                            dist[x][y - h] = yDistance
                                        }
                        }
                        1 -> {
                            if (y < height - h)
                                if (i < totalSize - h * width)
                                    if (colors[i + h * width] == Color.TRANSPARENT)
                                        if (dist[x][y + h] >= yDistance) {
                                            scaledInputBitmap.setPixel(x, y + h, newColor)
                                            dist[x][y + h] = yDistance
                                        }
                        }
                        2 -> {
                            if (y > h)
                                if (i >= h * width)
                                    if (colors[i - h * width] == Color.TRANSPARENT)
                                        if (dist[x][y - h] >= yDistance) {
                                            scaledInputBitmap.setPixel(x, y - h, newColor)
                                            dist[x][y - h] = yDistance
                                        }
                            if (y < height - h)
                                if (i < totalSize - h * width)
                                    if (colors[i + h * width] == Color.TRANSPARENT)
                                        if (dist[x][y + h] >= yDistance) {
                                            scaledInputBitmap.setPixel(x, y + h, newColor)
                                            dist[x][y + h] = yDistance
                                        }
                        }
                    }
                }
            } else {
                for (w in 0 until widthMax) {
                    for (h in 0 until heightMax) {
                        val xyDistance = distance(w, h);
                        val newColor =
                            Color.argb(
                                alpha * (distMax - distance(w, h).toInt()) / distMax,
                                red,
                                green,
                                blue
                            )
                        when (xDirection) {
                            0 -> {
                                if (x > w) {
                                    when (yDirection) {
                                        0 -> {
                                            if (y > h)
                                                if (i >= w + h * width)
                                                    if (colors[i - w - h * width] == Color.TRANSPARENT)
                                                        if (dist[x - w][y - h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x - w, y - h, newColor)
                                                            dist[x - w][y - h] = xyDistance
                                                        }
                                        }
                                        1 -> {
                                            if (y < height - h)
                                                if (i >= w - h * width && i < totalSize + w - h * width)
                                                    if (colors[i - w + h * width] == Color.TRANSPARENT)
                                                        if (dist[x - w][y + h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x - w, y + h, newColor)
                                                            dist[x - w][y + h] = xyDistance
                                                        }
                                        }
                                        2 -> {
                                            if (y > h)
                                                if (i >= w + h * height)
                                                    if (colors[i - w - h * width] == Color.TRANSPARENT)
                                                        if (dist[x - w][y - h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x - w, y - h, newColor)
                                                            dist[x - w][y - h] = xyDistance
                                                        }
                                            if (y < height - h)
                                                if (i >= w - h * width && i < totalSize + w - h * width)
                                                    if (colors[i - w + h * width] == Color.TRANSPARENT)
                                                        if (dist[x - w][y + h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x - w, y + h, newColor)
                                                            dist[x - w][y + h] = xyDistance
                                                        }
                                            Log.i("[xDyD]", "이런게 있네02")
                                        }
                                    }
                                }
                            }
                            1 -> {
                                if (x < width - w - 1) {
                                    when (yDirection) {
                                        0 -> {
                                            if (y > h)
                                                if (i >= -w + h * width && i < totalSize - w + h * width)
                                                    if (colors[i + w - h * width] == Color.TRANSPARENT)
                                                        if (dist[x + w][y - h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x + w, y - h, newColor)
                                                            dist[x + w][y - h] = xyDistance
                                                        }
                                        }
                                        1 -> {
                                            if (y < height - h)
                                                if (i < totalSize - w - h * width)
                                                    if (colors[i + w + h * width] == Color.TRANSPARENT)
                                                        if (dist[x + w][y + h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x + w, y + h, newColor)
                                                            dist[x + w][y + h] = xyDistance
                                                        }
                                        }
                                        2 -> {
                                            if (y > h)
                                                if (i >= -w + h * width && i < totalSize - w + h * width)
                                                    if (colors[i + w - h * width] == Color.TRANSPARENT)
                                                        if (dist[x + w][y - h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x + w, y - h, newColor)
                                                            dist[x + w][y - h] = xyDistance
                                                        }
                                            if (y < height - h)
                                                if (i < totalSize - w - h * width)
                                                    if (colors[i + w + h * width] == Color.TRANSPARENT)
                                                        if (dist[x + w][y + h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x + w, y + h, newColor)
                                                            dist[x + w][y + h] = xyDistance
                                                        }
                                            Log.i("[xDyD]", "이런게 있네12")
                                        }
                                    }
                                }
                            }
                            2 -> {
                                if (x > w) {
                                    when (yDirection) {
                                        0 -> {
                                            if (y > h)
                                                if (i >= w + h * width)
                                                    if (colors[i - w - h * width] == Color.TRANSPARENT)
                                                        if (dist[x - w][y - h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x - w, y - h, newColor)
                                                            dist[x - w][y - h] = xyDistance
                                                        }
                                        }
                                        1 -> {
                                            if (y < height - h)
                                                if (i >= w - h * width && i < totalSize + w - h * width)
                                                    if (colors[i - w + h * width] == Color.TRANSPARENT)
                                                        if (dist[x - w][y + h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x - w, y + h, newColor)
                                                            dist[x - w][y - h] = xyDistance
                                                        }
                                        }
                                        2 -> {
                                            if (y > h)
                                                if (i >= w + h * width)
                                                    if (colors[i - w - h * width] == Color.TRANSPARENT)
                                                        if (dist[x - w][y - h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x - w, y - h, newColor)
                                                            dist[x - w][y - h] = xyDistance
                                                        }
                                            if (y < height - h)
                                                if (i >= w - h * width && i < totalSize + w - h * width)
                                                    if (colors[i - w + h * width] == Color.TRANSPARENT)
                                                        if (dist[x - w][y + h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x - w, y + h, newColor)
                                                            dist[x - w][y + h] = xyDistance
                                                        }
                                            Log.i("[xDyD]", "이런게 있네22")
                                        }
                                    }
                                }
                                if (x < width - w) {
                                    when (yDirection) {
                                        0 -> {
                                            if (y > h)
                                                if (i >= -w + h * width && i < totalSize - w + h * width)
                                                    if (colors[i + w - h * width] == Color.TRANSPARENT)
                                                        if (dist[x + w][y - h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x + w, y - h, newColor)
                                                            dist[x + w][y - h] = xyDistance
                                                        }
                                        }
                                        1 -> {
                                            if (y < height - h)
                                                if (i < totalSize - w - h * width)
                                                    if (colors[i + w + h * width] == Color.TRANSPARENT)
                                                        if (dist[x + w][y + h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x + w, y + h, newColor)
                                                            dist[x + w][y + h] = xyDistance
                                                        }
                                        }
                                        2 -> {
                                            if (y > h)
                                                if (i >= -w + h * width && i < totalSize - w + h * width)
                                                    if (colors[i + w - h * width] == Color.TRANSPARENT)
                                                        if (dist[x + w][y - h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x + w, y - h, newColor)
                                                            dist[x + w][y - h] = xyDistance
                                                        }
                                            if (y < height - h)
                                                if (i < totalSize - w - h * width)
                                                    if (colors[i + w + h * width] == Color.TRANSPARENT)
                                                        if (dist[x + w][y + h] >= xyDistance) {
                                                            scaledInputBitmap.setPixel(x + w, y + h, newColor)
                                                            dist[x + w][y + h] = xyDistance
                                                        }
                                            Log.i("[xDyD]", "이런게 있네22")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (isScaled) {
            scaledInputBitmap = Bitmap.createScaledBitmap(scaledInputBitmap, inputImage.width, inputImage.height, true)
            val canvas = Canvas(scaledInputBitmap)
            canvas.drawBitmap(inputImage, 0f, 0f, null)
        }
        return scaledInputBitmap
    }

    private fun checkX(x: Int, i: Int): Int {
        when (x) {
            0 -> {
                return if (colors[i + 1] == Color.TRANSPARENT)
                    1
                else
                    -1
            }
            width - 1 -> {
                return if (colors[i - 1] == Color.TRANSPARENT)
                    0
                else
                    -1
            }
            else -> {
                return if (colors[i - 1] == Color.TRANSPARENT && colors[i + 1] == Color.TRANSPARENT)
                    2
                else if (colors[i - 1] == Color.TRANSPARENT)
                    0
                else if (colors[i + 1] == Color.TRANSPARENT)
                    1
                else
                    -1
            }
        }
    }

    private fun checkY(y: Int, i: Int): Int {
        when (y) {
            0 -> {
                return if (colors[i + width] == Color.TRANSPARENT)
                    1
                else
                    -1
            }
            height - 1 -> {
                return if (colors[i - width] == Color.TRANSPARENT)
                    0
                else
                    -1
            }
            else -> {
                return if (colors[i - width] == Color.TRANSPARENT && colors[i + width] == Color.TRANSPARENT)
                    2
                else if (colors[i - width] == Color.TRANSPARENT)
                    0
                else if (colors[i + width] == Color.TRANSPARENT)
                    1
                else
                    -1
            }
        }
    }

    private fun distance(x: Int, y: Int): Float {
        return sqrt(x.toFloat().pow(2) + y.toFloat().pow(2))
    }

    fun setWeight(w: Int) {
        weight = 110 - w
        weight = weight.coerceAtMost(width)
        weight = weight.coerceAtMost(height)
    }

    fun setColor(c: Int) {
        color = c
    }
}