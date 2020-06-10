package capstone.aiimageeditor

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.Math.max
import java.lang.Math.pow
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class ImageHalo() {
    lateinit var colors: IntArray
    private var weight: Int = 110

    private var coordQueue: Queue<Triple<Int, Int, Pair<Int, Int>>> = LinkedList()
    private var width: Int = 0
    private var height: Int = 0

    fun setHalo(inputImage: Bitmap, inputMask: Bitmap, color: Int): Bitmap {
        colors = IntArray(inputMask.width * inputMask.height)
        inputMask.getPixels(colors, 0, inputMask.width, 0, 0, inputMask.width, inputMask.height)
        width = inputMask.width
        height = inputMask.height
        val widthMax = inputMask.width / weight
        val heightMax = inputMask.height / weight
        val distMax = widthMax.coerceAtMost(heightMax)
        var dist: Array<Array<Float>> = Array(width) { Array(height) { distMax.toFloat() } }

        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        for (i in colors.indices) {
            if (colors[i] == Color.WHITE) {
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
                        Color.argb(255 * (widthMax - w) / widthMax, red, green, blue)
                    when (xDirection) {
                        0 -> {
                            if (x > w)
                                if (colors[i - w] != Color.WHITE)
                                    if (dist[x - w][y] >= wDistance) {
                                        inputImage.setPixel(x - w, y, newColor)
                                        dist[x - w][y] = wDistance;
                                    }
                        }
                        1 -> {
                            if (x < width - w - 1)
                                if (colors[i + w] != Color.WHITE)
                                    if (dist[x + w][y] >= wDistance) {
                                        inputImage.setPixel(x + w, y, newColor)
                                        dist[x + w][y] = wDistance;
                                    }
                        }
                        2 -> {
                            if (x > w)
                                if (colors[i - w] != Color.WHITE)
                                    if (dist[x - w][y] >= wDistance) {
                                        inputImage.setPixel(x - w, y, newColor)
                                        dist[x - w][y] = wDistance;
                                    }
                            if (x < width - w - 1)
                                if (colors[i + w] != Color.WHITE)
                                    if (dist[x + w][y] >= wDistance) {
                                        inputImage.setPixel(x + w, y, newColor)
                                        dist[x + w][y] = wDistance;
                                    }
                        }
                    }
                }
            } else if (xDirection == -1) {
                for (h in 0 until heightMax) {
                    val yDistance = distance(0, h)
                    val newColor =
                        Color.argb(255 * (heightMax - h) / heightMax, red, green, blue)
                    when (yDirection) {
                        0 -> {
                            if (y > h)
                                if (colors[i - h * width] != Color.WHITE)
                                    if (dist[x][y - h] >= yDistance) {
                                        inputImage.setPixel(x, y - h, newColor)
                                        dist[x][y - h] = yDistance
                                    }
                        }
                        1 -> {
                            if (y < height - h - 1)
                                if (colors[i + h * width] != Color.WHITE)
                                    if (dist[x][y + h] >= yDistance) {
                                        inputImage.setPixel(x, y + h, newColor)
                                        dist[x][y + h] = yDistance
                                    }
                        }
                        2 -> {
                            if (y > h)
                                if (colors[i - h * width] != Color.WHITE)
                                    if (dist[x][y - h] >= yDistance) {
                                        inputImage.setPixel(x, y - h, newColor)
                                        dist[x][y - h] = yDistance
                                    }
                            if (y < height - h - 1)
                                if (colors[i + h * width] != Color.WHITE)
                                    if (dist[x][y + h] >= yDistance) {
                                        inputImage.setPixel(x, y + h, newColor)
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
                                255 * (distMax - distance(w, h).toInt()) / distMax,
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
                                                if (colors[i - w - h * width] != Color.WHITE)
                                                    if (dist[x - w][y - h] >= xyDistance) {
                                                        inputImage.setPixel(x - w, y - h, newColor)
                                                        dist[x - w][y - h] = xyDistance
                                                    }
                                        }
                                        1 -> {
                                            if (y < height - h - 1)
                                                if (colors[i - w + h * width] != Color.WHITE)
                                                    if (dist[x - w][y + h] >= xyDistance) {
                                                        inputImage.setPixel(x - w, y + h, newColor)
                                                        dist[x - w][y + h] = xyDistance
                                                    }
                                        }
                                        2 -> {
                                            if (y > h)
                                                if (colors[i - w - h * width] != Color.WHITE)
                                                    if (dist[x - w][y - h] >= xyDistance) {
                                                        inputImage.setPixel(x - w, y - h, newColor)
                                                        dist[x - w][y - h] = xyDistance
                                                    }
                                            if (y < height - h - 1)
                                                if (colors[i - w + h * width] != Color.WHITE)
                                                    if (dist[x - w][y + h] >= xyDistance) {
                                                        inputImage.setPixel(x - w, y + h, newColor)
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
                                                if (colors[i + w - h * width] != Color.WHITE)
                                                    if (dist[x + w][y - h] >= xyDistance) {
                                                        inputImage.setPixel(x + w, y - h, newColor)
                                                        dist[x + w][y - h] = xyDistance
                                                    }
                                        }
                                        1 -> {
                                            if (y < height - h - 1)
                                                if (colors[i + w + h * width] != Color.WHITE)
                                                    if (dist[x + w][y + h] >= xyDistance) {
                                                        inputImage.setPixel(x + w, y + h, newColor)
                                                        dist[x + w][y + h] = xyDistance
                                                    }
                                        }
                                        2 -> {
                                            if (y > h)
                                                if (colors[i + w - h * width] != Color.WHITE)
                                                    if (dist[x + w][y - h] >= xyDistance) {
                                                        inputImage.setPixel(x + w, y - h, newColor)
                                                        dist[x + w][y - h] = xyDistance
                                                    }
                                            if (y < height - h - 1)
                                                if (colors[i + w + h * width] != Color.WHITE)
                                                    if (dist[x + w][y + h] >= xyDistance) {
                                                        inputImage.setPixel(x + w, y + h, newColor)
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
                                                if (colors[i - w - h * width] != Color.WHITE)
                                                    if (dist[x - w][y - h] >= xyDistance) {
                                                        inputImage.setPixel(x - w, y - h, newColor)
                                                        dist[x - w][y - h] = xyDistance
                                                    }
                                        }
                                        1 -> {
                                            if (y < height - h - 1)
                                                if (colors[i - w + h * width] != Color.WHITE)
                                                    if (dist[x - w][y + h] >= xyDistance) {
                                                        inputImage.setPixel(x - w, y + h, newColor)
                                                        dist[x - w][y - h] = xyDistance
                                                    }
                                        }
                                        2 -> {
                                            if (y > h)
                                                if (colors[i - w - h * width] != Color.WHITE)
                                                    if (dist[x - w][y - h] >= xyDistance) {
                                                        inputImage.setPixel(x - w, y - h, newColor)
                                                        dist[x - w][y - h] = xyDistance
                                                    }
                                            if (y < height - h - 1)
                                                if (colors[i - w + h * width] != Color.WHITE)
                                                    if (dist[x - w][y + h] >= xyDistance) {
                                                        inputImage.setPixel(x - w, y + h, newColor)
                                                        dist[x - w][y + h] = xyDistance
                                                    }
                                            Log.i("[xDyD]", "이런게 있네22")
                                        }
                                    }
                                }
                                if (x < width - w - 1) {
                                    when (yDirection) {
                                        0 -> {
                                            if (y > h)
                                                if (colors[i + w - h * width] != Color.WHITE)
                                                    if (dist[x + w][y - h] >= xyDistance) {
                                                        inputImage.setPixel(x + w, y - h, newColor)
                                                        dist[x + w][y - h] = xyDistance
                                                    }
                                        }
                                        1 -> {
                                            if (y < height - h - 1)
                                                if (colors[i + w + h * width] != Color.WHITE)
                                                    if (dist[x + w][y + h] >= xyDistance) {
                                                        inputImage.setPixel(x + w, y + h, newColor)
                                                        dist[x + w][y + h] = xyDistance
                                                    }
                                        }
                                        2 -> {
                                            if (y > h)
                                                if (colors[i + w - h * width] != Color.WHITE)
                                                    if (dist[x + w][y - h] >= xyDistance) {
                                                        inputImage.setPixel(x + w, y - h, newColor)
                                                        dist[x + w][y - h] = xyDistance
                                                    }
                                            if (y < height - h - 1)
                                                if (colors[i + w + h * width] != Color.WHITE)
                                                    if (dist[x + w][y + h] >= xyDistance) {
                                                        inputImage.setPixel(x + w, y + h, newColor)
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
        return inputImage
    }

    private fun checkX(x: Int, i: Int): Int {
        when (x) {
            0 -> {
                return if (colors[i + 1] != Color.WHITE)
                    1
                else
                    -1
            }
            height - 1 -> {
                return if (colors[i - 1] != Color.WHITE)
                    0
                else
                    -1
            }
            else -> {
                return if (colors[i - 1] != Color.WHITE && colors[i + 1] != Color.WHITE)
                    2
                else if (colors[i - 1] != Color.WHITE)
                    0
                else if (colors[i + 1] != Color.WHITE)
                    1
                else
                    -1
            }
        }
    }

    private fun checkY(y: Int, i: Int): Int {
        when (y) {
            0 -> {
                return if (colors[i + width] != Color.WHITE)
                    1
                else
                    -1
            }
            height - 1 -> {
                return if (colors[i - width] != Color.WHITE)
                    0
                else
                    -1
            }
            else -> {
                return if (colors[i - width] != Color.WHITE && colors[i + width] != Color.WHITE)
                    2
                else if (colors[i - width] != Color.WHITE)
                    0
                else if (colors[i + width] != Color.WHITE)
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
}