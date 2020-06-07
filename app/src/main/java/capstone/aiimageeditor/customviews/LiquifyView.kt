package capstone.aiimageeditor.customviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_MOVE
import android.view.View
import kotlin.math.pow


class LiquifyView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var meshWidth = 45
    private var meshHeight = 30
    private var xstart = 0f
    private var ystart = 0f

    private lateinit var sorted: List<Pair<Float, Float>>
    private lateinit var original: List<Pair<Float, Float>>
    private lateinit var selectedIndex: MutableList<Int>
    private var paint = Paint()
    private lateinit var coordinates: List<Pair<Float, Float>>
    private lateinit var bitmap: Bitmap
    private lateinit var bgimg: Bitmap
    private var mode: Int = 0


    private var _width:Int = 0
    private var _height:Int = 0
    private lateinit var drawCanvas: Canvas
    private lateinit var canvasBitmap: Bitmap


    fun setup(column: Int, row: Int, img: Bitmap, backgroundimg:Bitmap) {
        meshWidth = column
        meshHeight = row
        bitmap = img
        bgimg = backgroundimg
        _width = bgimg.width
        _height = bgimg.height
        selectedIndex = MutableList<Int>(300,{_ -> 0})
        paint.color = Color.BLACK

        generateCoordinates()
        invalidate() //ondraw호출
    }
    fun brushsizechange(size: Int) {
        mode = size
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (_width.toFloat() / _height.toFloat() > w.toFloat() / h.toFloat()) {
            _height = _height * w / _width
            _width = w
        } else {
            _width = _width * h / _height
            _height = h
        }

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap)

        generateCoordinates()
    }



    private fun generateCoordinates() {
        coordinates = generateCoordinate(
            meshWidth,
            meshHeight,
            _width,
            _height,
            paddingStart,
            paddingEnd,
            paddingTop,
            paddingBottom
        )
    }


    override fun onDraw(canvas: Canvas) {
        Log.i("##ondraw", "ondraw 실행")
        super.onDraw(canvas)

        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR)
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        //canvas.getContext("2d").clearRect(0, 0, canvas.width, canvas.height)


        drawCanvas.drawBitmapMesh(
            bitmap,
            meshWidth,
            meshHeight,     //2차원좌표를 1차원배열로 표현
            coordinates.flatMap { listOf(it.first, it.second) }.toFloatArray(),
            0,
            null,
            0,
            null
        )


        drawCoordinates(drawCanvas)
        drawLines(drawCanvas)



        canvas.drawBitmap(canvasBitmap, 0f, (height/2 - _height/2).toFloat(), Paint())
    }

    private fun drawCoordinates(canvas: Canvas) {
        coordinates.forEach {
            canvas.drawPoint(it.first, it.second, paint)
        }
    }

    private fun drawLines(canvas: Canvas) {

        coordinates.forEachIndexed { index, pair ->
            // Draw horizontal line with next column
            if (((index + 1) % (meshWidth + 1)) != 0) {
                val nextCoordinate = coordinates[index + 1]
                drawLine(canvas, pair, nextCoordinate)
            }

            // Draw horizontal line with next row
            if (((index < (meshWidth + 1) * meshHeight))) {
                val nextCoordinate = coordinates[index + meshWidth + 1]
                drawLine(canvas, pair, nextCoordinate)
            }
        }
    }

    private fun drawLine(
        canvas: Canvas,
        pair: Pair<Float, Float>,
        nextCoordinate: Pair<Float, Float>
    ) {
        canvas.drawLine(
            pair.first, pair.second,
            nextCoordinate.first, nextCoordinate.second,
            paint
        )
    }




    private fun isEdge(i: Int): Boolean {
        if (i >= 0 && i <= meshWidth) return true
        else if (i % (meshWidth + 1) == 0 || i % (meshWidth + 1) == meshWidth) return true
        else if (i >= (meshWidth + 1) * (meshHeight) && i <= (meshWidth + 1) * (meshHeight + 1) - 1) return true
        else return false
    }
    private fun weight(mode: Int, i: Int): Float {
        //size : 3x3, 5x5, 8x8, 15x15, 20x20
        var exp = arrayOf(0.1f, 0.05f,0.008f,0.004f,0.002f)
        var res: Float = (i+1).toFloat().pow(exp[mode])
        return 1 / res
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            ACTION_DOWN -> {
                xstart = event.x
                ystart = event.y //- _height/2
                original = coordinates
                sorted = coordinates.sortedBy { (it.first - xstart).pow(2) + (it.second - ystart).pow(2) }
                for (i in 0..250) {
                    val idx = coordinates.indexOf(sorted[i])
                    if (isEdge(idx)) selectedIndex[i] = -1 else selectedIndex[i] = idx
                }
                return true
            }
            ACTION_MOVE, ACTION_UP -> {
                var xmove = (event.x - xstart) / 20
                var ymove = (event.y - ystart) / 20

                for (i in 0..250) {
                    xmove *= weight(mode, i) //mode: 0~4
                    ymove *= weight(mode, i)
                    var s = selectedIndex[i]
                    coordinates =
                        coordinates.mapIndexed { index, pair -> if (index == s) ((original[s].first + xmove) to (original[s].second + ymove)) else pair }
                }
                invalidate()
                return true
            }
        }
        return false
    }

    private fun generateCoordinate(
        col: Int, row: Int, width: Int, height: Int,
        paddingStart: Int = 0, paddingEnd: Int = 0,
        paddingTop: Int = 0, paddingBottom: Int = 0
    ): List<Pair<Float, Float>> {

        val widthSlice = (width - (paddingStart + paddingEnd)) / (col)
        val heightSlice = (height - (paddingTop + paddingBottom)) / (row)

        val coordinates = mutableListOf<Pair<Float, Float>>()

        for (y in 0..row) {
            for (x in 0..col) {
                coordinates.add(
                    Pair(
                        (x * widthSlice + paddingStart).toFloat(),
                        (y * heightSlice + paddingTop).toFloat()
                    )
                )
            }
        }

        return coordinates
    }
}