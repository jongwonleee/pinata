package capstone.aiimageeditor

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*

class ImageHalo() {
    private lateinit var colors: IntArray

    private var coordQueue: Queue<Triple<Int, Int, Pair<Int, Int>>> = LinkedList()
    private var width: Int = 0
    private var height: Int = 0
    private var bool = false

    fun setHalo(inputImage: Bitmap, inputMask: Bitmap): Bitmap {
        colors = IntArray(inputMask.width * inputMask.height)
        inputMask.getPixels(colors, 0, inputMask.width, 0, 0, inputMask.width, inputMask.height)
        width = inputMask.width
        height = inputMask.height
        bool = false

        for (i in colors.indices) {
            if (colors[i] == Color.RED) {
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
            val i = y * inputMask.width + x
            val xDirection = temp.third.first
            val yDirection = temp.third.second
            val widthMax = inputMask.width / 75
            val heightMax = inputMask.height / 75
            val alphaMax = (widthMax + heightMax)

            if (yDirection == -1) {
                when (xDirection) {
                    0 -> {
                        for (w in 0 until widthMax) {
                            if (x > w) {
                                val newColor = Color.argb(255 * (widthMax - w) / widthMax, 0, 255, 255)
                                inputImage.setPixel(x - w, y, newColor)
                            }
                        }
                    }
                    1 -> {
                        for (w in 0 until widthMax) {
                            if (x < width - w - 1) {
                                val newColor = Color.argb(255 * (widthMax - w) / widthMax, 0, 255, 255)
                                inputImage.setPixel(x + w, y, newColor)
                            }
                        }
                    }
                    2 -> {
                        for (w in 0 until widthMax) {
                            if (x > w) {
                                val newColor = Color.argb(255 * (widthMax - w) / widthMax, 0, 255, 255)
                                inputImage.setPixel(x - w, y, newColor)
                            }
                            if (x < width - w - 1) {
                                val newColor = Color.argb(255 * (widthMax - w) / widthMax, 0, 255, 255)
                                inputImage.setPixel(x + w, y, newColor)
                            }
                        }
                    }
                }
            } else if (xDirection == -1) {
                when (yDirection) {
                    0 -> {
                        for (h in 0 until heightMax) {
                            if (y > h) {
                                val newColor = Color.argb(255 * (heightMax - h) / heightMax, 0, 255, 255)
                                inputImage.setPixel(x, y - h, newColor)
                            }
                        }
                    }
                    1 -> {
                        for (h in 0 until heightMax) {
                            if (y < height - h - 1) {
                                val newColor = Color.argb(255 * (heightMax - h) / heightMax, 0, 255, 255)
                                inputImage.setPixel(x, y + h, newColor)
                            }
                        }
                    }
                    2 -> {
                        for (h in 0 until heightMax) {
                            if (y > h) {
                                val newColor = Color.argb(255 * (heightMax - h) / heightMax, 0, 255, 255)
                                inputImage.setPixel(x, y - h, newColor)
                            }
                            if (y < height - h - 1) {
                                val newColor = Color.argb(255 * (heightMax - h) / heightMax, 0, 255, 255)
                                inputImage.setPixel(x, y + h, newColor)
                            }
                        }
                    }
                }
            } else {
                when (xDirection) {
                    0 -> {
                        for (w in 0 until widthMax) {
                            if (x > w) {
                                when (yDirection) {
                                    0 -> {
                                        for (h in 0 until heightMax) {
                                            if (y > h) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x - w, y - h, newColor)
                                            }
                                        }
                                    }
                                    1 -> {
                                        for (h in 0 until heightMax) {
                                            if (y < height - h - 1) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x - w, y + h, newColor)
                                            }
                                        }
                                    }
                                    2 -> {
                                        for (h in 0 until heightMax) {
                                            if (y > h) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x - w, y - h, newColor)
                                            }
                                            if (y < height - h - 1) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x - w, y + h, newColor)
                                            }
                                        }
                                        Log.i("[xDyD]", "이런게 있네02")
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        for (w in 0 until widthMax) {
                            if (x < width - w - 1) {
                                when (yDirection) {
                                    0 -> {
                                        for (h in 0 until heightMax) {
                                            if (y > h) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x + w, y - h, newColor)
                                            }
                                        }
                                    }
                                    1 -> {
                                        for (h in 0 until heightMax) {
                                            if (y < height - h - 1) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x + w, y + h, newColor)
                                            }
                                        }
                                    }
                                    2 -> {
                                        for (h in 0 until heightMax) {
                                            if (y > h) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x + w, y - h, newColor)
                                            }
                                            if (y < height - h - 1) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x + w, y + h, newColor)
                                            }
                                        }
                                        Log.i("[xDyD]", "이런게 있네12")
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        for (w in 0 until widthMax) {
                            if (x > w) {
                                when (yDirection) {
                                    0 -> {
                                        for (h in 0 until heightMax) {
                                            if (y > h) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x - w, y - h, newColor)
                                            }
                                        }
                                    }
                                    1 -> {
                                        for (h in 0 until heightMax) {
                                            if (y < height - h - 1) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x - w, y + h, newColor)
                                            }
                                        }
                                    }
                                    2 -> {
                                        for (h in 0 until heightMax) {
                                            if (y > h) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x - w, y - h, newColor)
                                            }
                                            if (y < height - h - 1) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x - w, y + h, newColor)
                                            }
                                        }
                                        Log.i("[xDyD]", "이런게 있네22")
                                    }
                                }
                            }
                            if (x < width - w - 1) {
                                when (yDirection) {
                                    0 -> {
                                        for (h in 0 until heightMax) {
                                            if (y > h) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x + w, y - h, newColor)
                                            }
                                        }
                                    }
                                    1 -> {
                                        for (h in 0 until heightMax) {
                                            if (y < height - h - 1) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x + w, y + h, newColor)
                                            }
                                        }
                                    }
                                    2 -> {
                                        for (h in 0 until heightMax) {
                                            if (y > h) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x + w, y - h, newColor)
                                            }
                                            if (y < height - h - 1) {
                                                val newColor = Color.argb(255 * (alphaMax - h - w) / alphaMax, 0, 255, 255)
                                                inputImage.setPixel(x + w, y + h, newColor)
                                            }
                                        }
                                        Log.i("[xDyD]", "이런게 있네12")
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
                return if (colors[i + 1] != Color.RED)
                    1
                else
                    -1
            }
            height - 1 -> {
                return if (colors[i - 1] != Color.RED)
                    0
                else
                    -1
            }
            else -> {
                return if (colors[i - 1] != Color.RED && colors[i + 1] != Color.RED)
                    2
                else if (colors[i - 1] != Color.RED)
                    0
                else if (colors[i + 1] != Color.RED)
                    1
                else
                    -1
            }
        }
    }

    private fun checkY(y: Int, i: Int): Int {
        when (y) {
            0 -> {
                return if (colors[i + width] != Color.RED)
                    1
                else
                    -1
            }
            height - 1 -> {
                return if (colors[i - width] != Color.RED)
                    0
                else
                    -1
            }
            else -> {
                return if (colors[i - width] != Color.RED && colors[i + width] != Color.RED)
                    2
                else if (colors[i - width] != Color.RED)
                    0
                else if (colors[i + width] != Color.RED)
                    1
                else
                    -1
            }
        }
    }
}