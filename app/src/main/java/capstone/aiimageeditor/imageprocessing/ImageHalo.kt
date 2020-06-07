package capstone.aiimageeditor

import android.graphics.Bitmap
import android.graphics.Color
import java.util.*

class ImageHalo(inputMask: Bitmap, inputImage: Bitmap) {

    private var mask = inputMask
    private var image = inputImage
    private var coordQueue: Queue<Pair<Int, Int>> = LinkedList<Pair<Int, Int>>()

    fun setHalo() {
        val colors = IntArray(mask.width * mask.height)
        mask.getPixels(colors, 0, mask.width, 0, 0, mask.width, mask.height)
        for (i in colors.indices) {
            val y = i / mask.width
            val x = i % mask.width
            if (Color.red(colors[i]) != 0) {
                if (i > 0 && Color.red(colors[i - 1]) != 0)
                    coordQueue.add(Pair<Int, Int>(y, x))
                else if (Color.red(colors[i + 1]) != 0)
                    coordQueue.add(Pair<Int, Int>(y, x))
                else if (i >= mask.width && Color.red(colors[i - mask.width]) != 0)
                    coordQueue.add(Pair<Int, Int>(y, x))
                else if (Color.red(colors[i + mask.width]) != 0)
                    coordQueue.add(Pair<Int, Int>(y, x))
                else if (i >= mask.width - 1 && Color.red(colors[i - mask.width + 1]) != 0)
                    coordQueue.add(Pair<Int, Int>(y, x))
                else if (Color.red(colors[i + mask.width + 1]) != 0)
                    coordQueue.add(Pair<Int, Int>(y, x))
                else if (i >= mask.width - 1 && Color.red(colors[i - mask.width - 1]) != 0)
                    coordQueue.add(Pair<Int, Int>(y, x))
                else if (Color.red(colors[i + mask.width - 1]) != 0)
                    coordQueue.add(Pair<Int, Int>(y, x))
            }
        }
        while (coordQueue.isNotEmpty()) {
            val temp = coordQueue.remove()
            val y = temp.first
            val x = temp.second
            val i = y * mask.width + x

            if (i > 0 && Color.red(colors[i - 1]) == 0)
                image.setPixel(x - 1, y, Color.WHITE)
            if (Color.red(colors[i + 1]) == 0)
                image.setPixel(x + 1, y, Color.WHITE)
            if (i >= mask.width && Color.red(colors[i - mask.width]) == 0)
                image.setPixel(x, y - 1, Color.WHITE)
            if (Color.red(colors[i + mask.width]) == 0)
                image.setPixel(x, y + 1, Color.WHITE)
            if (i >= mask.width - 1 && Color.red(colors[i - mask.width + 1]) == 0)
                image.setPixel(x + 1, y - 1, Color.WHITE)
            if (Color.red(colors[i + mask.width + 1]) == 0)
                image.setPixel(x + 1, y + 1, Color.WHITE)
            if (i >= mask.width - 1 && Color.red(colors[i - mask.width - 1]) == 0)
                image.setPixel(x - 1, y - 1, Color.WHITE)
            if (Color.red(colors[i + mask.width - 1]) == 0)
                image.setPixel(x - 1, y + 1, Color.WHITE)
        }
    }
}